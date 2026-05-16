package com.investigacion.data_management_demo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface StrainReadingMapper {

	@Insert("""
			INSERT IGNORE INTO strain_readings (
			  message_id,
			  project_id,
			  sensor_id,
			  timestamp,
			  received_at,
			  strain_microstrain,
			  temperature_celsius
			) VALUES (
			  #{messageId},
			  #{projectId},
			  #{sensorId},
			  #{timestamp},
			  #{receivedAt},
			  #{strainMicrostrain},
			  #{temperatureCelsius}
			)
			""")
	int insertIgnore(
			@Param("messageId") String messageId,
			@Param("projectId") String projectId,
			@Param("sensorId") String sensorId,
			@Param("timestamp") LocalDateTime timestamp,
			@Param("receivedAt") LocalDateTime receivedAt,
			@Param("strainMicrostrain") Double strainMicrostrain,
			@Param("temperatureCelsius") Double temperatureCelsius
	);
}
