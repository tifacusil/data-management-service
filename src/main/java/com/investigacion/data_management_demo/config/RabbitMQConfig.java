package com.investigacion.data_management_demo.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	@Bean
	public SimpleRabbitListenerContainerFactory manualAckContainerFactory(
			ConnectionFactory connectionFactory,
			@Value("${app.rabbitmq.listeners.enabled:true}") boolean listenersEnabled,
			@Value("${app.rabbitmq.listeners.concurrency:4}") int concurrency,
			@Value("${app.rabbitmq.listeners.max-concurrency:8}") int maxConcurrency,
			@Value("${app.rabbitmq.listeners.prefetch:50}") int prefetch
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setDefaultRequeueRejected(false);
		factory.setAutoStartup(listenersEnabled);
		// Cada consumidor procesa un INSERT sincrónico contra MySQL remoto; sin
		// concurrencia el throughput queda limitado a 1 mensaje por RTT de la BD.
		factory.setConcurrentConsumers(concurrency);
		factory.setMaxConcurrentConsumers(maxConcurrency);
		factory.setPrefetchCount(prefetch);
		return factory;
	}
}
