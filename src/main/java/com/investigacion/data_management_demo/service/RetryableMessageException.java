package com.investigacion.data_management_demo.service;

public class RetryableMessageException extends RuntimeException {
	public RetryableMessageException(String message, Throwable cause) {
		super(message, cause);
	}
}
