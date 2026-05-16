package com.investigacion.data_management_demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investigacion.data_management_demo.model.DeepDisplacementPayload;
import com.investigacion.data_management_demo.model.Envelope;
import com.investigacion.data_management_demo.model.GnssPayload;
import com.investigacion.data_management_demo.model.RainfallPayload;
import com.investigacion.data_management_demo.model.SoilPressurePayload;
import com.investigacion.data_management_demo.model.StrainPayload;
import org.springframework.stereotype.Service;

@Service
public class MessageRouter {
	private final ObjectMapper objectMapper;
	private final PersistenceService persistenceService;

	public MessageRouter(ObjectMapper objectMapper, PersistenceService persistenceService) {
		this.objectMapper = objectMapper;
		this.persistenceService = persistenceService;
	}

	public void route(Envelope envelope) {
		if (envelope == null) {
			throw new NonRetryableMessageException("Envelope nulo");
		}
		if (envelope.sensorType() == null || envelope.sensorType().isBlank()) {
			throw new NonRetryableMessageException("sensor_type vacio");
		}
		if (envelope.payload() == null) {
			throw new NonRetryableMessageException("payload nulo");
		}

		try {
			switch (envelope.sensorType()) {
				case "gnss" -> persistenceService.persistGnss(envelope, objectMapper.treeToValue(envelope.payload(), GnssPayload.class));
				case "rainfall" ->
						persistenceService.persistRainfall(envelope, objectMapper.treeToValue(envelope.payload(), RainfallPayload.class));
				case "strain" -> persistenceService.persistStrain(envelope, objectMapper.treeToValue(envelope.payload(), StrainPayload.class));
				case "soil_pressure" ->
						persistenceService.persistSoilPressure(envelope, objectMapper.treeToValue(envelope.payload(), SoilPressurePayload.class));
				case "deep_displacement" ->
						persistenceService.persistDeepDisplacement(envelope, objectMapper.treeToValue(envelope.payload(), DeepDisplacementPayload.class));
				default -> throw new NonRetryableMessageException("sensor_type no soportado: " + envelope.sensorType());
			}
		} catch (JsonProcessingException ex) {
			throw new NonRetryableMessageException("Fallo parseo payload", ex);
		}
	}
}
