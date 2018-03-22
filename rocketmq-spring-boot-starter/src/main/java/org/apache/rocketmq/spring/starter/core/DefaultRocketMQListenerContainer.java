/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.spring.starter.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.starter.enums.ConsumeMode;
import org.apache.rocketmq.spring.starter.enums.SelectorType;
import org.apache.rocketmq.spring.starter.exception.ConvertMsgException;
import org.apache.rocketmq.spring.starter.msgvo.ConsumeFailedMsgVO;
import org.apache.rocketmq.spring.starter.utils.IPUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("WeakerAccess")
@Slf4j
public class DefaultRocketMQListenerContainer implements InitializingBean, RocketMQListenerContainer {

    @Setter
    @Getter
    private long suspendCurrentQueueTimeMillis = 1000;

    /**
     * Message consume retry strategy<br> -1,no retry,put into DLQ directly<br> 0,broker control retry frequency<br>
     * >0,client control retry frequency
     */
    @Setter
    @Getter
    private int delayLevelWhenNextConsume = 0;

    @Setter
    @Getter
    private String consumerGroup;

    @Setter
    @Getter
    private String nameServer;

    @Setter
    @Getter
    private String topic;

    @Setter
    @Getter
    private ConsumeMode consumeMode = ConsumeMode.CONCURRENTLY;

    @Setter
    @Getter
    private SelectorType selectorType = SelectorType.TAG;

    @Setter
    @Getter
    private String selectorExpress = "*";

    @Setter
    @Getter
    private MessageModel messageModel = MessageModel.CLUSTERING;

    @Setter
    @Getter
    private int consumeThreadMax = 64;

    @Getter
    @Setter
    private String charset = "UTF-8";

    @Setter
    @Getter
    private ObjectMapper objectMapper = new ObjectMapper();

    @Setter
    @Getter
    private boolean started;

    @Setter
    private RocketMQListener rocketMQListener;

    private DefaultMQPushConsumer consumer;

    private Class messageType;

    @Setter
    private RocketMQTemplate rocketMQTemplate;
    
    public void setupMessageListener(RocketMQListener rocketMQListener) {
        this.rocketMQListener = rocketMQListener;
    }

    @Override
    public void destroy() {
        this.setStarted(false);
        if (Objects.nonNull(consumer)) {
            consumer.shutdown();
        }
        log.info("container destroyed, {}", this.toString());
    }

    public synchronized void start() throws MQClientException {

        if (this.isStarted()) {
            throw new IllegalStateException("container already started. " + this.toString());
        }

        initRocketMQPushConsumer();

        // parse message type
        this.messageType = getMessageType();
        log.debug("msgType: {}", messageType.getName());

        consumer.start();
        this.setStarted(true);

        log.info("started container: {}", this.toString());
    }

    public class DefaultMessageListenerConcurrently implements MessageListenerConcurrently {

        @SuppressWarnings("unchecked")
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            for (MessageExt messageExt : msgs) {
            	Date consumeBeginTime = new Date();
                log.debug("received msg: {}", messageExt);
                try {
                    long now = System.currentTimeMillis();
                    rocketMQListener.onMessage(doConvertMessage(messageExt));
                    long costTime = System.currentTimeMillis() - now;
                    log.debug("consume {} cost: {} ms", messageExt.getMsgId(), costTime);
                } catch (Exception e) {
                    log.warn("consume message failed. messageExt:{}", messageExt, e);
                    context.setDelayLevelWhenNextConsume(delayLevelWhenNextConsume);
                    if(messageExt.getTopic().equals("DATA_COLLECTION_TOPIC") && "ConsumeMsgFailed".equals(messageExt.getTags())){
    					log.error("消费失败的消息为“保存消费失败日志消息”，不需要记录日志,不需要重新消费，直接返回成功");
    					return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    				}
                    if(e instanceof ConvertMsgException){
                    	log.error("消费失败的原因为转换对象失败，需要记录日志，不需要重新消费，返回消费成功");
                    	//消息消费失败，发送失败消息
                    	this.sendConsumeMsgFailed(messageExt,e,consumeBeginTime);
                    	return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                	this.sendConsumeMsgFailed(messageExt,e,consumeBeginTime);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        /**
         * 发送消息消费失败消息
         * @param messageExt
         * @param e
         * 2018年3月22日 zhaowg
         */
		private void sendConsumeMsgFailed(MessageExt messageExt, Exception e,Date consumeBeginTime) {
			log.info("消费消息失败，开始发送消费失败MQ");
			String topic = "DATA_COLLECTION_TOPIC";
			String tag  = "ConsumeMsgFailed";
			try{
				Date consumeEndTime = new Date();
				String destination = topic+":"+tag;
				ConsumeFailedMsgVO consumeFailedMsgVO = new ConsumeFailedMsgVO();
				consumeFailedMsgVO.setConsumeBeginTime(consumeBeginTime);
				consumeFailedMsgVO.setConsumeEndTime(consumeEndTime);
				consumeFailedMsgVO.setConsumeGroup(consumerGroup);
				consumeFailedMsgVO.setConsumeIp(IPUtil.getLocalHost());
				if(e!=null){
					String errMsg = ExceptionUtils.getStackTrace(e);
					if(StringUtils.isNotBlank(errMsg)){
						//最多保存1024个字符
						consumeFailedMsgVO.setCunsumerErrMsg(errMsg.substring(0, 1024));
					}
				}
				consumeFailedMsgVO.setMsg(new String(messageExt.getBody()));
				consumeFailedMsgVO.setMsgId(messageExt.getMsgId());
				consumeFailedMsgVO.setMsgKeys(messageExt.getKeys());
				consumeFailedMsgVO.setReconsumeTimes(messageExt.getReconsumeTimes());
				consumeFailedMsgVO.setTag(messageExt.getTags());
				consumeFailedMsgVO.setTopic(messageExt.getTopic());
				rocketMQTemplate.sendOneWay(destination, consumeFailedMsgVO);
				log.info("发送消息消费失败MQ成功");
			}catch(Exception e1){
				log.info("发送消息消费失败MQ异常",e);
			}
			
		}
    }

    public class DefaultMessageListenerOrderly implements MessageListenerOrderly {

        @SuppressWarnings("unchecked")
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
            for (MessageExt messageExt : msgs) {
                log.debug("received msg: {}", messageExt);
                try {
                    long now = System.currentTimeMillis();
                    rocketMQListener.onMessage(doConvertMessage(messageExt));
                    long costTime = System.currentTimeMillis() - now;
                    log.info("consume {} cost: {} ms", messageExt.getMsgId(), costTime);
                } catch (Exception e) {
                    log.warn("consume message failed. messageExt:{}", messageExt, e);
                    context.setSuspendCurrentQueueTimeMillis(suspendCurrentQueueTimeMillis);
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }

            return ConsumeOrderlyStatus.SUCCESS;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public String toString() {
        return "DefaultRocketMQListenerContainer{" +
            "consumerGroup='" + consumerGroup + '\'' +
            ", nameServer='" + nameServer + '\'' +
            ", topic='" + topic + '\'' +
            ", consumeMode=" + consumeMode +
            ", selectorType=" + selectorType +
            ", selectorExpress='" + selectorExpress + '\'' +
            ", messageModel=" + messageModel +
            '}';
    }

    @SuppressWarnings("unchecked")
    private Object doConvertMessage(MessageExt messageExt) {
        if (Objects.equals(messageType, MessageExt.class)) {
            return messageExt;
        } else {
            String str = new String(messageExt.getBody(), Charset.forName(charset));
            if (Objects.equals(messageType, String.class)) {
                return str;
            } else {
                // if msgType not string, use objectMapper change it.
                try {
                    return objectMapper.readValue(str, messageType);
                } catch (Exception e) {
                    log.info("convert failed. str:{}, msgType:{}", str, messageType);
                    throw new ConvertMsgException("cannot convert message to " + messageType, e);
                }
            }
        }
    }

    private Class getMessageType() {
        Type[] interfaces = rocketMQListener.getClass().getGenericInterfaces();
        if (Objects.nonNull(interfaces)) {
            for (Type type : interfaces) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    if (Objects.equals(parameterizedType.getRawType(), RocketMQListener.class)) {
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
                            return (Class) actualTypeArguments[0];
                        } else {
                            return Object.class;
                        }
                    }
                }
            }

            return Object.class;
        } else {
            return Object.class;
        }
    }

    private void initRocketMQPushConsumer() throws MQClientException {

        Assert.notNull(rocketMQListener, "Property 'rocketMQListener' is required");
        Assert.notNull(consumerGroup, "Property 'consumerGroup' is required");
        Assert.notNull(nameServer, "Property 'nameServer' is required");
        Assert.notNull(topic, "Property 'topic' is required");

        consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeThreadMax(consumeThreadMax);
        if (consumeThreadMax < consumer.getConsumeThreadMin()) {
            consumer.setConsumeThreadMin(consumeThreadMax);
        }

        consumer.setMessageModel(messageModel);

        switch (selectorType) {
            case TAG:
                consumer.subscribe(topic, selectorExpress);
                break;
            case SQL92:
                consumer.subscribe(topic, MessageSelector.bySql(selectorExpress));
                break;
            default:
                throw new IllegalArgumentException("Property 'selectorType' was wrong.");
        }

        switch (consumeMode) {
            case ORDERLY:
                consumer.setMessageListener(new DefaultMessageListenerOrderly());
                break;
            case CONCURRENTLY:
                consumer.setMessageListener(new DefaultMessageListenerConcurrently());
                break;
            default:
                throw new IllegalArgumentException("Property 'consumeMode' was wrong.");
        }

        // provide an entryway to custom setting RocketMQ consumer
        if (rocketMQListener instanceof RocketMQPushConsumerLifecycleListener) {
            ((RocketMQPushConsumerLifecycleListener) rocketMQListener).prepareStart(consumer);
        }

    }

}
