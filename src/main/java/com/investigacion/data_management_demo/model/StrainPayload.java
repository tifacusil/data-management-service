package com.investigacion.data_management_demo.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StrainPayload(
		@JsonAlias({ "strain_microstrain", "strainMicrostrain" }) Double strainMicrostrain,
		@JsonAlias({ "temperature_celsius", "temperatureCelsius" }) Double temperatureCelsius
) {
}
