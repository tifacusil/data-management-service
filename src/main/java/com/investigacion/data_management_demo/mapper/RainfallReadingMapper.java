package com.investigacion.data_management_demo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface RainfallReadingMapper {

	@Insert("""
			INSERT IGNORE INTO rainfall_readings (
			  message_id,
			  project_id,
			  sensor_id,
			  timestamp,
			  received_at,
			  rainfall_mm,
			  cumulative_rainfall_mm,
			  interval_minutes
			) VALUES (
			  #{messageId},
			  #{projectId},
			  #{sensorId},
			  #{timestamp},
			  #{receivedAt},
			  #{rainfallMm},
			  #{cumulativeRainfallMm},
			  #{intervalMinutes}
			)
			""")
	int insertIgnore(
			@Param("messageId") String messageId,
			@Param("projectId") String projectId,
			@Param("sensorId") String sensorId,
			@Param("timestamp") LocalDateTime timestamp,
			@Param("receivedAt") LocalDateTime receivedAt,
			@Param("rainfallMm") Double rainfallMm,
			@Param("cumulativeRainfallMm") Double cumulativeRainfallMm,
			@Param("intervalMinutes") Integer intervalMinutes
	);
}
