package com.investigacion.data_management_demo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface DeepDisplacementReadingMapper {

	@Insert("""
			INSERT IGNORE INTO deep_displacement_readings (
			  message_id,
			  project_id,
			  sensor_id,
			  timestamp,
			  received_at,
			  depth_m,
			  tilt_x_mm,
			  tilt_y_mm,
			  cumulative_tilt_mm
			) VALUES (
			  #{messageId},
			  #{projectId},
			  #{sensorId},
			  #{timestamp},
			  #{receivedAt},
			  #{depthM},
			  #{tiltXmm},
			  #{tiltYmm},
			  #{cumulativeTiltMm}
			)
			""")
	int insertIgnore(
			@Param("messageId") String messageId,
			@Param("projectId") String projectId,
			@Param("sensorId") String sensorId,
			@Param("timestamp") LocalDateTime timestamp,
			@Param("receivedAt") LocalDateTime receivedAt,
			@Param("depthM") Double depthM,
			@Param("tiltXmm") Double tiltXmm,
			@Param("tiltYmm") Double tiltYmm,
			@Param("cumulativeTiltMm") Double cumulativeTiltMm
	);
}
