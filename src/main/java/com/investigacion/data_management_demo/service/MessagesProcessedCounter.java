package com.investigacion.data_management_demo.service;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class MessagesProcessedCounter {
	private final AtomicLong processed = new AtomicLong();

	public MessagesProcessedCounter(MeterRegistry registry) {
		// FunctionCounter (y no Counter) para que el AtomicLong siga siendo la unica
		// fuente de verdad: /health y /metrics leen el mismo contador y no pueden
		// divergir. Micrometer publica "dm.messages.processed" como
		// dm_messages_processed_total, el nombre que ya esperan Prometheus y los
		// scripts de chaos del Sistema A.
		FunctionCounter.builder("dm.messages.processed", processed, AtomicLong::get)
				.description("Mensajes consumidos de RabbitMQ y persistidos.")
				.register(registry);
	}

	public long get() {
		return processed.get();
	}

	public void increment() {
		processed.incrementAndGet();
	}
}
