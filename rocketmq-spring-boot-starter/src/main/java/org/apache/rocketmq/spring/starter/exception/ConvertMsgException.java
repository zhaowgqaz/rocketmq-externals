package org.apache.rocketmq.spring.starter.exception;

public class ConvertMsgException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public ConvertMsgException() {
		super();
	}

	public ConvertMsgException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConvertMsgException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConvertMsgException(String message) {
		super(message);
	}

	public ConvertMsgException(Throwable cause) {
		super(cause);
	}
	
}
