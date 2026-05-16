package com.investigacion.data_management_demo.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SoilPressurePayload(
		@JsonAlias({ "pressure_kpa", "pressureKpa" }) Double pressureKpa,
		@JsonAlias({ "temperature_celsius", "temperatureCelsius" }) Double temperatureCelsius
) {
}
