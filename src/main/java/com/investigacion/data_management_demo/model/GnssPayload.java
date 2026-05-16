package com.investigacion.data_management_demo.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GnssPayload(
		@JsonAlias({ "displacement_x_mm", "displacementXmm" }) Double displacementXmm,
		@JsonAlias({ "displacement_y_mm", "displacementYmm" }) Double displacementYmm,
		@JsonAlias({ "displacement_z_mm", "displacementZmm" }) Double displacementZmm,
		@JsonAlias({ "displacement_horizontal_mm", "displacementHorizontalMm" }) Double displacementHorizontalMm,
		@JsonAlias({ "cumulative_displacement_mm", "cumulativeDisplacementMm" }) Double cumulativeDisplacementMm,
		@JsonAlias({ "fix_quality", "fixQuality" }) String fixQuality
) {
}
