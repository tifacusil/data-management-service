package com.investigacion.data_management_demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.investigacion.data_management_demo.mapper.GnssReadingMapper;
import com.investigacion.data_management_demo.service.MessagesProcessedCounter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
public class HealthController {
	private static final int DEFAULT_LIMIT = 200;
	private static final int MAX_LIMIT = 1000;
	private static final Set<String> SUPPORTED_SENSOR_TYPES = Set.of("gnss", "rainfall", "strain", "soil_pressure", "deep_displacement");

	private final MessagesProcessedCounter counter;
	private final GnssReadingMapper readingsMapper;
	private final ObjectMapper objectMapper;
	private final DataSource dataSource;
	private final ConnectionFactory rabbitConnectionFactory;

	public HealthController(
			MessagesProcessedCounter counter,
			GnssReadingMapper readingsMapper,
			ObjectMapper objectMapper,
			DataSource dataSource,
			ConnectionFactory rabbitConnectionFactory
	) {
		this.counter = counter;
		this.readingsMapper = readingsMapper;
		this.objectMapper = objectMapper;
		this.dataSource = dataSource;
		this.rabbitConnectionFactory = rabbitConnectionFactory;
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> health() {
		CheckResult mysql = checkMysql();
		CheckResult rabbit = checkRabbit();
		boolean healthy = mysql.ok && rabbit.ok;

		Map<String, Object> body = Map.of(
				"status", healthy ? "healthy" : "unhealthy",
				"mysql", mysql.toMap(),
				"rabbitmq", rabbit.toMap(),
				"messages_processed", counter.get()
		);
		return ResponseEntity.status(healthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
	}

	private CheckResult checkMysql() {
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement ps = conn.prepareStatement("SELECT 1");
			 ResultSet rs = ps.executeQuery()) {
			if (!rs.next()) {
				return CheckResult.fail("SELECT 1 sin filas");
			}
			int v = rs.getInt(1);
			return v == 1 ? CheckResult.success() : CheckResult.fail("SELECT 1 != 1");
		} catch (Exception ex) {
			return CheckResult.fail(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	private CheckResult checkRabbit() {
		try (org.springframework.amqp.rabbit.connection.Connection conn = rabbitConnectionFactory.createConnection()) {
			return conn.isOpen() ? CheckResult.success() : CheckResult.fail("conexion cerrada");
		} catch (Exception ex) {
			return CheckResult.fail(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	@GetMapping("/api/v1/sensors/{sensor_id}/readings")
	public PagedResponse<ReadingDto> sensorReadings(
			@PathVariable("sensor_id") String sensorId,
			@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(value = "sensor_type", required = false) String sensorType,
			@RequestParam(value = "fix_quality", required = false) String fixQuality,
			@RequestParam(value = "offset", defaultValue = "0") int offset,
			@RequestParam(value = "limit", required = false) Integer limit
	) {
		int resolvedLimit = resolveLimit(limit);
		validateTimeRange(from, to);
		String resolvedSensorType = normalizeSensorType(sensorType);
		validateSensorType(resolvedSensorType);
		validateFixQuality(resolvedSensorType, fixQuality);

		LocalDateTime fromUtc = toUtc(from);
		LocalDateTime toUtc = toUtc(to);

		int resolvedOffset = resolveOffset(offset);
		int probeLimit = Math.min(resolvedLimit + 1, MAX_LIMIT + 1);
		List<Map<String, Object>> rows = readingsMapper.selectSensorReadingsPage(
				sensorId,
				fromUtc,
				toUtc,
				resolvedSensorType,
				fixQuality,
				resolvedOffset,
				probeLimit
		);
		boolean hasMore = rows.size() > resolvedLimit;
		List<Map<String, Object>> pageRows = hasMore ? rows.subList(0, resolvedLimit) : rows;
		long total = hasMore ? readingsMapper.countSensorReadings(sensorId, fromUtc, toUtc, resolvedSensorType, fixQuality)
				: (long) resolvedOffset + pageRows.size();
		return new PagedResponse<>(total, resolvedOffset, resolvedLimit, pageRows.stream().map(this::toReadingDto).toList());
	}

	@GetMapping("/api/v1/projects/{project_id}/readings")
	public PagedResponse<ReadingDto> projectReadings(
			@PathVariable("project_id") String projectId,
			@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(value = "sensor_type", required = false) String sensorType,
			@RequestParam(value = "fix_quality", required = false) String fixQuality,
			@RequestParam(value = "offset", defaultValue = "0") int offset,
			@RequestParam(value = "limit", required = false) Integer limit
	) {
		int resolvedLimit = resolveLimit(limit);
		validateTimeRange(from, to);
		String resolvedSensorType = normalizeSensorType(sensorType);
		validateSensorType(resolvedSensorType);
		validateFixQuality(resolvedSensorType, fixQuality);

		LocalDateTime fromUtc = toUtc(from);
		LocalDateTime toUtc = toUtc(to);

		int resolvedOffset = resolveOffset(offset);
		int probeLimit = Math.min(resolvedLimit + 1, MAX_LIMIT + 1);
		List<Map<String, Object>> rows = readingsMapper.selectProjectReadingsPage(
				projectId,
				fromUtc,
				toUtc,
				resolvedSensorType,
				fixQuality,
				resolvedOffset,
				probeLimit
		);
		boolean hasMore = rows.size() > resolvedLimit;
		List<Map<String, Object>> pageRows = hasMore ? rows.subList(0, resolvedLimit) : rows;
		long total = hasMore ? readingsMapper.countProjectReadings(projectId, fromUtc, toUtc, resolvedSensorType, fixQuality)
				: (long) resolvedOffset + pageRows.size();
		return new PagedResponse<>(total, resolvedOffset, resolvedLimit, pageRows.stream().map(this::toReadingDto).toList());
	}

	@GetMapping("/api/v1/projects/{project_id}/latest")
	public List<ReadingDto> projectLatest(
			@PathVariable("project_id") String projectId,
			@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(value = "sensor_type", required = false) String sensorType,
			@RequestParam(value = "fix_quality", required = false) String fixQuality
	) {
		validateTimeRange(from, to);
		String resolvedSensorType = normalizeSensorType(sensorType);
		validateSensorType(resolvedSensorType);
		validateFixQuality(resolvedSensorType, fixQuality);

		LocalDateTime fromUtc = toUtc(from);
		LocalDateTime toUtc = toUtc(to);

		List<Map<String, Object>> rows = readingsMapper.selectLatestByProject(projectId, fromUtc, toUtc, resolvedSensorType, fixQuality);
		return rows.stream().map(this::toReadingDto).toList();
	}

	private static int resolveOffset(int offset) {
		if (offset < 0) {
			throw new ResponseStatusException(BAD_REQUEST, "offset debe ser >= 0");
		}
		return offset;
	}

	private static int resolveLimit(Integer limit) {
		int resolved = limit == null ? DEFAULT_LIMIT : limit;
		if (resolved <= 0) {
			throw new ResponseStatusException(BAD_REQUEST, "limit debe ser > 0");
		}
		if (resolved > MAX_LIMIT) {
			throw new ResponseStatusException(BAD_REQUEST, "limit excede maximo: " + MAX_LIMIT);
		}
		return resolved;
	}

	private static void validateTimeRange(Instant from, Instant to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new ResponseStatusException(BAD_REQUEST, "from debe ser <= to");
		}
	}

	private static String normalizeSensorType(String sensorType) {
		if (sensorType == null || sensorType.isBlank()) {
			return null;
		}
		return sensorType.trim().toLowerCase();
	}

	private static void validateSensorType(String sensorType) {
		if (sensorType == null) {
			return;
		}
		if (!SUPPORTED_SENSOR_TYPES.contains(sensorType)) {
			throw new ResponseStatusException(BAD_REQUEST, "sensor_type no soportado: " + sensorType);
		}
	}

	private static void validateFixQuality(String sensorType, String fixQuality) {
		if (fixQuality == null || fixQuality.isBlank()) {
			return;
		}
		if (sensorType != null && !"gnss".equals(sensorType)) {
			throw new ResponseStatusException(BAD_REQUEST, "fix_quality solo aplica para sensor_type=gnss");
		}
	}

	private static LocalDateTime toUtc(Instant instant) {
		if (instant == null) {
			return null;
		}
		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
	}

	private ReadingDto toReadingDto(Map<String, Object> row) {
		String projectId = (String) row.get("projectId");
		String sensorId = (String) row.get("sensorId");
		String sensorType = (String) row.get("sensorType");
		Instant timestamp = toInstant(row.get("timestamp"));
		Instant receivedAt = toInstant(row.get("receivedAt"));
		JsonNode payload = buildPayload(sensorType, row);
		return new ReadingDto(projectId, sensorId, sensorType, timestamp, receivedAt, payload);
	}

	private JsonNode buildPayload(String sensorType, Map<String, Object> row) {
		if (sensorType == null) {
			return null;
		}
		ObjectNode payload = objectMapper.createObjectNode();
		switch (sensorType) {
			case "gnss" -> {
				putIfNotNull(payload, "displacement_x_mm", toDouble(row.get("displacementXmm")));
				putIfNotNull(payload, "displacement_y_mm", toDouble(row.get("displacementYmm")));
				putIfNotNull(payload, "displacement_z_mm", toDouble(row.get("displacementZmm")));
				putIfNotNull(payload, "displacement_horizontal_mm", toDouble(row.get("displacementHorizontalMm")));
				putIfNotNull(payload, "cumulative_displacement_mm", toDouble(row.get("cumulativeDisplacementMm")));
				putIfNotNull(payload, "fix_quality", toStringValue(row.get("fixQuality")));
			}
			case "rainfall" -> {
				putIfNotNull(payload, "rainfall_mm", toDouble(row.get("rainfallMm")));
				putIfNotNull(payload, "cumulative_rainfall_mm", toDouble(row.get("cumulativeRainfallMm")));
				putIfNotNull(payload, "interval_minutes", toInteger(row.get("intervalMinutes")));
			}
			case "strain" -> {
				putIfNotNull(payload, "strain_microstrain", toDouble(row.get("strainMicrostrain")));
				putIfNotNull(payload, "temperature_celsius", toDouble(row.get("temperatureCelsius")));
			}
			case "soil_pressure" -> {
				putIfNotNull(payload, "pressure_kpa", toDouble(row.get("pressureKpa")));
				putIfNotNull(payload, "temperature_celsius", toDouble(row.get("temperatureCelsius")));
			}
			case "deep_displacement" -> {
				putIfNotNull(payload, "depth_m", toDouble(row.get("depthM")));
				putIfNotNull(payload, "tilt_x_mm", toDouble(row.get("tiltXmm")));
				putIfNotNull(payload, "tilt_y_mm", toDouble(row.get("tiltYmm")));
				putIfNotNull(payload, "cumulative_tilt_mm", toDouble(row.get("cumulativeTiltMm")));
			}
			default -> {
			}
		}
		return payload;
	}

	private static void putIfNotNull(ObjectNode node, String field, Double value) {
		if (value != null) {
			node.put(field, value);
		}
	}

	private static void putIfNotNull(ObjectNode node, String field, Integer value) {
		if (value != null) {
			node.put(field, value);
		}
	}

	private static void putIfNotNull(ObjectNode node, String field, String value) {
		if (value != null) {
			node.put(field, value);
		}
	}

	private static Double toDouble(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number n) {
			return n.doubleValue();
		}
		if (value instanceof String s && !s.isBlank()) {
			return Double.valueOf(s);
		}
		return null;
	}

	private static Integer toInteger(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number n) {
			return n.intValue();
		}
		if (value instanceof String s && !s.isBlank()) {
			return Integer.valueOf(s);
		}
		return null;
	}

	private static String toStringValue(Object value) {
		if (value == null) {
			return null;
		}
		String s = String.valueOf(value);
		return s.isBlank() ? null : s;
	}

	private static Instant toInstant(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Timestamp ts) {
			return ts.toInstant();
		}
		if (value instanceof LocalDateTime ldt) {
			return ldt.toInstant(ZoneOffset.UTC);
		}
		if (value instanceof Instant instant) {
			return instant;
		}
		throw new IllegalArgumentException("Tipo de fecha/hora no soportado: " + value.getClass().getName());
	}

	public record ReadingDto(
			String projectId,
			String sensorId,
			String sensorType,
			Instant timestamp,
			Instant receivedAt,
			JsonNode payload
	) {
	}

	public record PagedResponse<T>(long total, int offset, int limit, List<T> items) {
	}

	private record CheckResult(boolean ok, String error) {
		static CheckResult success() {
			return new CheckResult(true, null);
		}

		static CheckResult fail(String error) {
			return new CheckResult(false, error == null || error.isBlank() ? "error" : error);
		}

		Map<String, Object> toMap() {
			return ok ? Map.of("ok", true) : Map.of("ok", false, "error", error);
		}
	}
}
