package org.apache.rocketmq.spring.starter.msgvo;

import java.io.Serializable;
import java.util.Date;

public class ConsumeFailedMsgVO implements Serializable{

	private static final long serialVersionUID = 1L;

	/**消息ID*/
    private String msgId;

    /**消息主题*/
    private String topic;

    /**消息标签描述*/
    private String tag;

    /**消费组*/
    private String consumeGroup;

    /**消费者ip*/
    private String consumeIp;

    /**消费开始时间*/
    private Date consumeBeginTime;

    /**消费结束时间*/
    private Date consumeEndTime;

    /**消息关键字*/
    private String msgKeys;

    /**重复消费次数*/
    private Integer reconsumeTimes;
    
    /**消费失败错误信息*/
    private String cunsumerErrMsg;

    /**消息内容*/
    private String msg;

    public String getCunsumerErrMsg() {
		return cunsumerErrMsg;
	}

	public void setCunsumerErrMsg(String cunsumerErrMsg) {
		this.cunsumerErrMsg = cunsumerErrMsg;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**获取消息ID*/
    public String getMsgId() {
        return msgId;
    }

    /**设置消息ID*/
    public void setMsgId(String msgId) {
        this.msgId = msgId == null ? null : msgId.trim();
    }

    /**获取消息主题*/
    public String getTopic() {
        return topic;
    }

    /**设置消息主题*/
    public void setTopic(String topic) {
        this.topic = topic == null ? null : topic.trim();
    }

    /**获取消息标签描述*/
    public String getTag() {
        return tag;
    }

    /**设置消息标签描述*/
    public void setTag(String tag) {
        this.tag = tag == null ? null : tag.trim();
    }

    /**获取消费组*/
    public String getConsumeGroup() {
        return consumeGroup;
    }

    /**设置消费组*/
    public void setConsumeGroup(String consumeGroup) {
        this.consumeGroup = consumeGroup == null ? null : consumeGroup.trim();
    }

    /**获取消费者ip*/
    public String getConsumeIp() {
        return consumeIp;
    }

    /**设置消费者ip*/
    public void setConsumeIp(String consumeIp) {
        this.consumeIp = consumeIp == null ? null : consumeIp.trim();
    }

    /**获取消费开始时间*/
    public Date getConsumeBeginTime() {
        return consumeBeginTime;
    }

    /**设置消费开始时间*/
    public void setConsumeBeginTime(Date consumeBeginTime) {
        this.consumeBeginTime = consumeBeginTime;
    }

    /**获取消费结束时间*/
    public Date getConsumeEndTime() {
        return consumeEndTime;
    }

    /**设置消费结束时间*/
    public void setConsumeEndTime(Date consumeEndTime) {
        this.consumeEndTime = consumeEndTime;
    }

    /**获取消息关键字*/
    public String getMsgKeys() {
        return msgKeys;
    }

    /**设置消息关键字*/
    public void setMsgKeys(String msgKeys) {
        this.msgKeys = msgKeys == null ? null : msgKeys.trim();
    }

    /**获取重复消费次数*/
    public Integer getReconsumeTimes() {
        return reconsumeTimes;
    }

    /**设置重复消费次数*/
    public void setReconsumeTimes(Integer reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

}
