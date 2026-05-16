package com.investigacion.data_management_demo.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class MessagesProcessedCounter {
	private final AtomicLong processed = new AtomicLong();

	public long get() {
		return processed.get();
	}

	public void increment() {
		processed.incrementAndGet();
	}
}
