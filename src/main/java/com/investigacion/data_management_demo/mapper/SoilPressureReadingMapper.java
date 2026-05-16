package com.investigacion.data_management_demo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SoilPressureReadingMapper {

	@Insert("""
			INSERT IGNORE INTO soil_pressure_readings (
			  message_id,
			  project_id,
			  sensor_id,
			  timestamp,
			  received_at,
			  pressure_kpa,
			  temperature_celsius
			) VALUES (
			  #{messageId},
			  #{projectId},
			  #{sensorId},
			  #{timestamp},
			  #{receivedAt},
			  #{pressureKpa},
			  #{temperatureCelsius}
			)
			""")
	int insertIgnore(
			@Param("messageId") String messageId,
			@Param("projectId") String projectId,
			@Param("sensorId") String sensorId,
			@Param("timestamp") LocalDateTime timestamp,
			@Param("receivedAt") LocalDateTime receivedAt,
			@Param("pressureKpa") Double pressureKpa,
			@Param("temperatureCelsius") Double temperatureCelsius
	);
}
