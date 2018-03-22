package org.apache.rocketmq.spring.starter.msgvo;

import java.io.Serializable;
import java.util.Date;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;

public class ConsumeFailedMsgVO implements Serializable{

	private static final long serialVersionUID = 1L;
	/**消息ID*/
    private String msgId;

    /**消费组*/
    private String consumeGroup;

    /**消费者ip*/
    private String consumeIp;

    /**消费组接受消息时间*/
    private Date consumeBeginTime;
    
    /**消费组处理完成时间*/
    private Date consumeEndTime;
    
    /**消费处理时长（秒）*/
    private Long executeTime;
    
    private ConsumeConcurrentlyStatus consumeConcurrentlyStatus;
    
    /**消费失败错误信息*/
    private Throwable e;

    /**消息*/
    private MessageExt messageExt;

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getConsumeGroup() {
		return consumeGroup;
	}

	public void setConsumeGroup(String consumeGroup) {
		this.consumeGroup = consumeGroup;
	}

	public String getConsumeIp() {
		return consumeIp;
	}

	public void setConsumeIp(String consumeIp) {
		this.consumeIp = consumeIp;
	}

	public Date getConsumeBeginTime() {
		return consumeBeginTime;
	}

	public void setConsumeBeginTime(Date consumeBeginTime) {
		this.consumeBeginTime = consumeBeginTime;
	}

	public Date getConsumeEndTime() {
		return consumeEndTime;
	}

	public void setConsumeEndTime(Date consumeEndTime) {
		this.consumeEndTime = consumeEndTime;
	}

	public Long getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(Long executeTime) {
		this.executeTime = executeTime;
	}

	public ConsumeConcurrentlyStatus getConsumeConcurrentlyStatus() {
		return consumeConcurrentlyStatus;
	}

	public void setConsumeConcurrentlyStatus(ConsumeConcurrentlyStatus consumeConcurrentlyStatus) {
		this.consumeConcurrentlyStatus = consumeConcurrentlyStatus;
	}

	public Throwable getE() {
		return e;
	}

	public void setE(Throwable e) {
		this.e = e;
	}

	public MessageExt getMessageExt() {
		return messageExt;
	}

	public void setMessageExt(MessageExt messageExt) {
		this.messageExt = messageExt;
	}
}
