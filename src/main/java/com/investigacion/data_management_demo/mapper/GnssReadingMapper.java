package com.investigacion.data_management_demo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface GnssReadingMapper {

	@Insert("""
			INSERT IGNORE INTO gnss_readings (
			  message_id,
			  project_id,
			  sensor_id,
			  timestamp,
			  received_at,
			  displacement_x_mm,
			  displacement_y_mm,
			  displacement_z_mm,
			  displacement_horizontal_mm,
			  cumulative_displacement_mm,
			  fix_quality
			) VALUES (
			  #{messageId},
			  #{projectId},
			  #{sensorId},
			  #{timestamp},
			  #{receivedAt},
			  #{displacementXmm},
			  #{displacementYmm},
			  #{displacementZmm},
			  #{displacementHorizontalMm},
			  #{cumulativeDisplacementMm},
			  #{fixQuality}
			)
			""")
	int insertIgnore(
			@Param("messageId") String messageId,
			@Param("projectId") String projectId,
			@Param("sensorId") String sensorId,
			@Param("timestamp") LocalDateTime timestamp,
			@Param("receivedAt") LocalDateTime receivedAt,
			@Param("displacementXmm") Double displacementXmm,
			@Param("displacementYmm") Double displacementYmm,
			@Param("displacementZmm") Double displacementZmm,
			@Param("displacementHorizontalMm") Double displacementHorizontalMm,
			@Param("cumulativeDisplacementMm") Double cumulativeDisplacementMm,
			@Param("fixQuality") String fixQuality
	);

	@Select("""
			<script>
			SELECT *
			FROM (
			  <choose>
			    <when test="sensorType != null and sensorType == 'gnss'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'gnss' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        displacement_x_mm AS displacementXmm,
			        displacement_y_mm AS displacementYmm,
			        displacement_z_mm AS displacementZmm,
			        displacement_horizontal_mm AS displacementHorizontalMm,
			        cumulative_displacement_mm AS cumulativeDisplacementMm,
			        fix_quality AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM gnss_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'rainfall'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'rainfall' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        rainfall_mm AS rainfallMm,
			        cumulative_rainfall_mm AS cumulativeRainfallMm,
			        interval_minutes AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM rainfall_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'strain'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'strain' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        strain_microstrain AS strainMicrostrain,
			        temperature_celsius AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM strain_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'soil_pressure'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'soil_pressure' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        temperature_celsius AS temperatureCelsius,
			        pressure_kpa AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM soil_pressure_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'deep_displacement'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'deep_displacement' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        depth_m AS depthM,
			        tilt_x_mm AS tiltXmm,
			        tilt_y_mm AS tiltYmm,
			        cumulative_tilt_mm AS cumulativeTiltMm
			      FROM deep_displacement_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <otherwise>
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'gnss' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        displacement_x_mm AS displacementXmm,
			        displacement_y_mm AS displacementYmm,
			        displacement_z_mm AS displacementZmm,
			        displacement_horizontal_mm AS displacementHorizontalMm,
			        cumulative_displacement_mm AS cumulativeDisplacementMm,
			        fix_quality AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM gnss_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			      UNION ALL
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'rainfall' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        rainfall_mm AS rainfallMm,
			        cumulative_rainfall_mm AS cumulativeRainfallMm,
			        interval_minutes AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM rainfall_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'strain' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        strain_microstrain AS strainMicrostrain,
			        temperature_celsius AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM strain_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'soil_pressure' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        temperature_celsius AS temperatureCelsius,
			        pressure_kpa AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM soil_pressure_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'deep_displacement' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        depth_m AS depthM,
			        tilt_x_mm AS tiltXmm,
			        tilt_y_mm AS tiltYmm,
			        cumulative_tilt_mm AS cumulativeTiltMm
			      FROM deep_displacement_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </otherwise>
			  </choose>
			) r
			ORDER BY timestamp DESC
			LIMIT #{limit} OFFSET #{offset}
			</script>
			""")
	List<Map<String, Object>> selectSensorReadingsPage(
			@Param("sensorId") String sensorId,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to,
			@Param("sensorType") String sensorType,
			@Param("fixQuality") String fixQuality,
			@Param("offset") int offset,
			@Param("limit") int limit
	);

	@Select("""
			<script>
			SELECT COUNT(*) AS total
			FROM (
			  <choose>
			    <when test="sensorType != null and sensorType == 'gnss'">
			      SELECT 1
			      FROM gnss_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'rainfall'">
			      SELECT 1
			      FROM rainfall_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'strain'">
			      SELECT 1
			      FROM strain_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'soil_pressure'">
			      SELECT 1
			      FROM soil_pressure_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'deep_displacement'">
			      SELECT 1
			      FROM deep_displacement_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <otherwise>
			      SELECT 1
			      FROM gnss_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			      UNION ALL
			      SELECT 1
			      FROM rainfall_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT 1
			      FROM strain_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT 1
			      FROM soil_pressure_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT 1
			      FROM deep_displacement_readings
			      WHERE sensor_id = #{sensorId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </otherwise>
			  </choose>
			) r
			</script>
			""")
	long countSensorReadings(
			@Param("sensorId") String sensorId,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to,
			@Param("sensorType") String sensorType,
			@Param("fixQuality") String fixQuality
	);

	@Select("""
			<script>
			SELECT *
			FROM (
			  <choose>
			    <when test="sensorType != null and sensorType == 'gnss'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'gnss' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        displacement_x_mm AS displacementXmm,
			        displacement_y_mm AS displacementYmm,
			        displacement_z_mm AS displacementZmm,
			        displacement_horizontal_mm AS displacementHorizontalMm,
			        cumulative_displacement_mm AS cumulativeDisplacementMm,
			        fix_quality AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM gnss_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'rainfall'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'rainfall' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        rainfall_mm AS rainfallMm,
			        cumulative_rainfall_mm AS cumulativeRainfallMm,
			        interval_minutes AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM rainfall_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'strain'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'strain' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        strain_microstrain AS strainMicrostrain,
			        temperature_celsius AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM strain_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'soil_pressure'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'soil_pressure' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        temperature_celsius AS temperatureCelsius,
			        pressure_kpa AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM soil_pressure_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'deep_displacement'">
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'deep_displacement' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        depth_m AS depthM,
			        tilt_x_mm AS tiltXmm,
			        tilt_y_mm AS tiltYmm,
			        cumulative_tilt_mm AS cumulativeTiltMm
			      FROM deep_displacement_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <otherwise>
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'gnss' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        displacement_x_mm AS displacementXmm,
			        displacement_y_mm AS displacementYmm,
			        displacement_z_mm AS displacementZmm,
			        displacement_horizontal_mm AS displacementHorizontalMm,
			        cumulative_displacement_mm AS cumulativeDisplacementMm,
			        fix_quality AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM gnss_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			      UNION ALL
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'rainfall' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        rainfall_mm AS rainfallMm,
			        cumulative_rainfall_mm AS cumulativeRainfallMm,
			        interval_minutes AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM rainfall_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'strain' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        strain_microstrain AS strainMicrostrain,
			        temperature_celsius AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM strain_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'soil_pressure' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        temperature_celsius AS temperatureCelsius,
			        pressure_kpa AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM soil_pressure_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT
			        project_id AS projectId,
			        sensor_id AS sensorId,
			        'deep_displacement' AS sensorType,
			        timestamp AS timestamp,
			        received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        depth_m AS depthM,
			        tilt_x_mm AS tiltXmm,
			        tilt_y_mm AS tiltYmm,
			        cumulative_tilt_mm AS cumulativeTiltMm
			      FROM deep_displacement_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </otherwise>
			  </choose>
			) r
			ORDER BY timestamp DESC, sensorId ASC, sensorType ASC
			LIMIT #{limit} OFFSET #{offset}
			</script>
			""")
	List<Map<String, Object>> selectProjectReadingsPage(
			@Param("projectId") String projectId,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to,
			@Param("sensorType") String sensorType,
			@Param("fixQuality") String fixQuality,
			@Param("offset") int offset,
			@Param("limit") int limit
	);

	@Select("""
			<script>
			SELECT COUNT(*) AS total
			FROM (
			  <choose>
			    <when test="sensorType != null and sensorType == 'gnss'">
			      SELECT 1
			      FROM gnss_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'rainfall'">
			      SELECT 1
			      FROM rainfall_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'strain'">
			      SELECT 1
			      FROM strain_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'soil_pressure'">
			      SELECT 1
			      FROM soil_pressure_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <when test="sensorType != null and sensorType == 'deep_displacement'">
			      SELECT 1
			      FROM deep_displacement_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </when>
			    <otherwise>
			      SELECT 1
			      FROM gnss_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			      UNION ALL
			      SELECT 1
			      FROM rainfall_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT 1
			      FROM strain_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT 1
			      FROM soil_pressure_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			      UNION ALL
			      SELECT 1
			      FROM deep_displacement_readings
			      WHERE project_id = #{projectId}
			      <if test="from != null">AND timestamp &gt;= #{from}</if>
			      <if test="to != null">AND timestamp &lt;= #{to}</if>
			    </otherwise>
			  </choose>
			) r
			</script>
			""")
	long countProjectReadings(
			@Param("projectId") String projectId,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to,
			@Param("sensorType") String sensorType,
			@Param("fixQuality") String fixQuality
	);

	@Select("""
			<script>
			SELECT *
			FROM (
			  <trim prefixOverrides="UNION ALL">
			    <if test="sensorType == null or sensorType == '' or sensorType == 'gnss'">
			      UNION ALL
			      SELECT
			        g.project_id AS projectId,
			        g.sensor_id AS sensorId,
			        'gnss' AS sensorType,
			        g.timestamp AS timestamp,
			        g.received_at AS receivedAt,
			        g.displacement_x_mm AS displacementXmm,
			        g.displacement_y_mm AS displacementYmm,
			        g.displacement_z_mm AS displacementZmm,
			        g.displacement_horizontal_mm AS displacementHorizontalMm,
			        g.cumulative_displacement_mm AS cumulativeDisplacementMm,
			        g.fix_quality AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM gnss_readings g
			      JOIN (
			        SELECT sensor_id, MAX(timestamp) AS max_timestamp
			        FROM gnss_readings
			        WHERE project_id = #{projectId}
			        <if test="from != null">AND timestamp &gt;= #{from}</if>
			        <if test="to != null">AND timestamp &lt;= #{to}</if>
			        <if test="fixQuality != null and fixQuality != ''">AND fix_quality = #{fixQuality}</if>
			        GROUP BY sensor_id
			      ) m ON g.sensor_id = m.sensor_id AND g.timestamp = m.max_timestamp
			      WHERE g.project_id = #{projectId}
			      <if test="from != null">AND g.timestamp &gt;= #{from}</if>
			      <if test="to != null">AND g.timestamp &lt;= #{to}</if>
			      <if test="fixQuality != null and fixQuality != ''">AND g.fix_quality = #{fixQuality}</if>
			    </if>
			    <if test="sensorType == null or sensorType == '' or sensorType == 'rainfall'">
			      UNION ALL
			      SELECT
			        r.project_id AS projectId,
			        r.sensor_id AS sensorId,
			        'rainfall' AS sensorType,
			        r.timestamp AS timestamp,
			        r.received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        r.rainfall_mm AS rainfallMm,
			        r.cumulative_rainfall_mm AS cumulativeRainfallMm,
			        r.interval_minutes AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM rainfall_readings r
			      JOIN (
			        SELECT sensor_id, MAX(timestamp) AS max_timestamp
			        FROM rainfall_readings
			        WHERE project_id = #{projectId}
			        <if test="from != null">AND timestamp &gt;= #{from}</if>
			        <if test="to != null">AND timestamp &lt;= #{to}</if>
			        GROUP BY sensor_id
			      ) m ON r.sensor_id = m.sensor_id AND r.timestamp = m.max_timestamp
			      WHERE r.project_id = #{projectId}
			      <if test="from != null">AND r.timestamp &gt;= #{from}</if>
			      <if test="to != null">AND r.timestamp &lt;= #{to}</if>
			    </if>
			    <if test="sensorType == null or sensorType == '' or sensorType == 'strain'">
			      UNION ALL
			      SELECT
			        s.project_id AS projectId,
			        s.sensor_id AS sensorId,
			        'strain' AS sensorType,
			        s.timestamp AS timestamp,
			        s.received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        s.strain_microstrain AS strainMicrostrain,
			        s.temperature_celsius AS temperatureCelsius,
			        NULL AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM strain_readings s
			      JOIN (
			        SELECT sensor_id, MAX(timestamp) AS max_timestamp
			        FROM strain_readings
			        WHERE project_id = #{projectId}
			        <if test="from != null">AND timestamp &gt;= #{from}</if>
			        <if test="to != null">AND timestamp &lt;= #{to}</if>
			        GROUP BY sensor_id
			      ) m ON s.sensor_id = m.sensor_id AND s.timestamp = m.max_timestamp
			      WHERE s.project_id = #{projectId}
			      <if test="from != null">AND s.timestamp &gt;= #{from}</if>
			      <if test="to != null">AND s.timestamp &lt;= #{to}</if>
			    </if>
			    <if test="sensorType == null or sensorType == '' or sensorType == 'soil_pressure'">
			      UNION ALL
			      SELECT
			        sp.project_id AS projectId,
			        sp.sensor_id AS sensorId,
			        'soil_pressure' AS sensorType,
			        sp.timestamp AS timestamp,
			        sp.received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        sp.temperature_celsius AS temperatureCelsius,
			        sp.pressure_kpa AS pressureKpa,
			        NULL AS depthM,
			        NULL AS tiltXmm,
			        NULL AS tiltYmm,
			        NULL AS cumulativeTiltMm
			      FROM soil_pressure_readings sp
			      JOIN (
			        SELECT sensor_id, MAX(timestamp) AS max_timestamp
			        FROM soil_pressure_readings
			        WHERE project_id = #{projectId}
			        <if test="from != null">AND timestamp &gt;= #{from}</if>
			        <if test="to != null">AND timestamp &lt;= #{to}</if>
			        GROUP BY sensor_id
			      ) m ON sp.sensor_id = m.sensor_id AND sp.timestamp = m.max_timestamp
			      WHERE sp.project_id = #{projectId}
			      <if test="from != null">AND sp.timestamp &gt;= #{from}</if>
			      <if test="to != null">AND sp.timestamp &lt;= #{to}</if>
			    </if>
			    <if test="sensorType == null or sensorType == '' or sensorType == 'deep_displacement'">
			      UNION ALL
			      SELECT
			        d.project_id AS projectId,
			        d.sensor_id AS sensorId,
			        'deep_displacement' AS sensorType,
			        d.timestamp AS timestamp,
			        d.received_at AS receivedAt,
			        NULL AS displacementXmm,
			        NULL AS displacementYmm,
			        NULL AS displacementZmm,
			        NULL AS displacementHorizontalMm,
			        NULL AS cumulativeDisplacementMm,
			        NULL AS fixQuality,
			        NULL AS rainfallMm,
			        NULL AS cumulativeRainfallMm,
			        NULL AS intervalMinutes,
			        NULL AS strainMicrostrain,
			        NULL AS temperatureCelsius,
			        NULL AS pressureKpa,
			        d.depth_m AS depthM,
			        d.tilt_x_mm AS tiltXmm,
			        d.tilt_y_mm AS tiltYmm,
			        d.cumulative_tilt_mm AS cumulativeTiltMm
			      FROM deep_displacement_readings d
			      JOIN (
			        SELECT sensor_id, MAX(timestamp) AS max_timestamp
			        FROM deep_displacement_readings
			        WHERE project_id = #{projectId}
			        <if test="from != null">AND timestamp &gt;= #{from}</if>
			        <if test="to != null">AND timestamp &lt;= #{to}</if>
			        GROUP BY sensor_id
			      ) m ON d.sensor_id = m.sensor_id AND d.timestamp = m.max_timestamp
			      WHERE d.project_id = #{projectId}
			      <if test="from != null">AND d.timestamp &gt;= #{from}</if>
			      <if test="to != null">AND d.timestamp &lt;= #{to}</if>
			    </if>
			  </trim>
			) latest
			ORDER BY sensorType ASC, sensorId ASC
			</script>
			""")
	List<Map<String, Object>> selectLatestByProject(
			@Param("projectId") String projectId,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to,
			@Param("sensorType") String sensorType,
			@Param("fixQuality") String fixQuality
	);
}
