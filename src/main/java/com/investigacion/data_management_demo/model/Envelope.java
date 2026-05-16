package com.investigacion.data_management_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Envelope(
		@JsonProperty("schema_version") String schemaVersion,
		@JsonProperty("message_id") String messageId,
		@JsonProperty("project_id") String projectId,
		@JsonProperty("sensor_id") String sensorId,
		@JsonProperty("sensor_type") String sensorType,
		@JsonProperty("timestamp") Instant timestamp,
		@JsonProperty("received_at") Instant receivedAt,
		@JsonProperty("sampling_interval_seconds") Integer samplingIntervalSeconds,
		@JsonProperty("payload") JsonNode payload
) {
}
