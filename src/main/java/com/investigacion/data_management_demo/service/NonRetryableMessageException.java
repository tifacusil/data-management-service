package com.investigacion.data_management_demo.service;

public class NonRetryableMessageException extends RuntimeException {
	public NonRetryableMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public NonRetryableMessageException(String message) {
		super(message);
	}
}
