package com.spring.shiro.redis.cache;

public class ShiroRedisException extends RuntimeException{

	private static final long serialVersionUID = 183930348563274008L;

	public ShiroRedisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ShiroRedisException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShiroRedisException(String message) {
		super(message);
	}

}
