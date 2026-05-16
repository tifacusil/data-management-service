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
			@Value("${app.rabbitmq.listeners.enabled:true}") boolean listenersEnabled
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setDefaultRequeueRejected(false);
		factory.setAutoStartup(listenersEnabled);
		return factory;
	}
}
