package com.investigacion.data_management_demo.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeepDisplacementPayload(
		@JsonAlias({ "depth_m", "depthM" }) Double depthM,
		@JsonAlias({ "tilt_x_mm", "tiltXmm" }) Double tiltXmm,
		@JsonAlias({ "tilt_y_mm", "tiltYmm" }) Double tiltYmm,
		@JsonAlias({ "cumulative_tilt_mm", "cumulativeTiltMm" }) Double cumulativeTiltMm
) {
}
