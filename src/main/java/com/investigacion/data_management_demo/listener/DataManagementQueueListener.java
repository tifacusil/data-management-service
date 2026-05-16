package com.investigacion.data_management_demo.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investigacion.data_management_demo.model.Envelope;
import com.investigacion.data_management_demo.service.MessageRouter;
import com.investigacion.data_management_demo.service.MessagesProcessedCounter;
import com.investigacion.data_management_demo.service.NonRetryableMessageException;
import com.investigacion.data_management_demo.service.RetryableMessageException;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class DataManagementQueueListener {
	private final ObjectMapper objectMapper;
	private final MessageRouter messageRouter;
	private final MessagesProcessedCounter counter;

	public DataManagementQueueListener(ObjectMapper objectMapper, MessageRouter messageRouter, MessagesProcessedCounter counter) {
		this.objectMapper = objectMapper;
		this.messageRouter = messageRouter;
		this.counter = counter;
	}

	@RabbitListener(queues = "data_management.persist", containerFactory = "manualAckContainerFactory")
	public void onMessage(Message message, Channel channel) throws IOException {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();
		try {
			String body = new String(message.getBody(), StandardCharsets.UTF_8);
			Envelope envelope = objectMapper.readValue(body, Envelope.class);
			messageRouter.route(envelope);
			channel.basicAck(deliveryTag, false);
			counter.increment();
		} catch (JsonProcessingException | NonRetryableMessageException ex) {
			channel.basicNack(deliveryTag, false, false);
		} catch (RetryableMessageException ex) {
			channel.basicNack(deliveryTag, false, true);
		} catch (Exception ex) {
			channel.basicNack(deliveryTag, false, true);
		}
	}
}
