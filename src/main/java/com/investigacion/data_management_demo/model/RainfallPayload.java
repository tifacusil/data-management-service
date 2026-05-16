package com.investigacion.data_management_demo.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RainfallPayload(
		@JsonAlias({ "rainfall_mm", "rainfallMm" }) Double rainfallMm,
		@JsonAlias({ "cumulative_rainfall_mm", "cumulativeRainfallMm" }) Double cumulativeRainfallMm,
		@JsonAlias({ "interval_minutes", "intervalMinutes" }) Integer intervalMinutes
) {
}
