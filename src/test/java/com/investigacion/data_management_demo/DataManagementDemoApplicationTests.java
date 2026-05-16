package com.investigacion.data_management_demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.investigacion.data_management_demo.mapper.GnssReadingMapper;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(
		classes = DataManagementDemoApplication.class,
		properties = {
				"app.rabbitmq.listeners.enabled=false"
		},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataManagementDemoApplicationTests {

	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
			.withDatabaseName("landslide_monitoring")
			.withUsername("test")
			.withPassword("test")
			.withUrlParam("rewriteBatchedStatements", "true")
			.withUrlParam("useServerPrepStmts", "true")
			.withUrlParam("cachePrepStmts", "true");

	static {
		mysql.start();
	}

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
	}

	@TestConfiguration
	static class TestRabbitConnectionConfig {
		@Bean
		ConnectionFactory connectionFactory() {
			return Mockito.mock(ConnectionFactory.class);
		}
	}

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	GnssReadingMapper readingsMapper;

	@BeforeAll
	void setupSchema() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS gnss_readings");
		jdbcTemplate.execute("DROP TABLE IF EXISTS rainfall_readings");
		jdbcTemplate.execute("DROP TABLE IF EXISTS strain_readings");
		jdbcTemplate.execute("DROP TABLE IF EXISTS soil_pressure_readings");
		jdbcTemplate.execute("DROP TABLE IF EXISTS deep_displacement_readings");

		jdbcTemplate.execute("""
				CREATE TABLE gnss_readings (
				  message_id VARCHAR(64) NOT NULL,
				  project_id VARCHAR(64) NOT NULL,
				  sensor_id VARCHAR(64) NOT NULL,
				  timestamp DATETIME(3) NOT NULL,
				  received_at DATETIME(3) NOT NULL,
				  displacement_x_mm DOUBLE NULL,
				  displacement_y_mm DOUBLE NULL,
				  displacement_z_mm DOUBLE NULL,
				  displacement_horizontal_mm DOUBLE NULL,
				  cumulative_displacement_mm DOUBLE NULL,
				  fix_quality VARCHAR(32) NULL,
				  PRIMARY KEY (message_id, timestamp),
				  KEY idx_gnss_sensor_time (sensor_id, timestamp),
				  KEY idx_gnss_project_time (project_id, timestamp),
				  KEY idx_gnss_sensor_fix_time (sensor_id, fix_quality, timestamp)
				) ENGINE=InnoDB
				PARTITION BY RANGE COLUMNS(timestamp) (
				  PARTITION p2025 VALUES LESS THAN ('2026-01-01'),
				  PARTITION p2026 VALUES LESS THAN ('2027-01-01'),
				  PARTITION pmax VALUES LESS THAN (MAXVALUE)
				)
				""");

		jdbcTemplate.execute("""
				CREATE TABLE rainfall_readings (
				  message_id VARCHAR(64) NOT NULL,
				  project_id VARCHAR(64) NOT NULL,
				  sensor_id VARCHAR(64) NOT NULL,
				  timestamp DATETIME(3) NOT NULL,
				  received_at DATETIME(3) NOT NULL,
				  rainfall_mm DOUBLE NULL,
				  cumulative_rainfall_mm DOUBLE NULL,
				  interval_minutes INT NULL,
				  PRIMARY KEY (message_id, timestamp),
				  KEY idx_rain_sensor_time (sensor_id, timestamp),
				  KEY idx_rain_project_time (project_id, timestamp)
				) ENGINE=InnoDB
				PARTITION BY RANGE COLUMNS(timestamp) (
				  PARTITION p2025 VALUES LESS THAN ('2026-01-01'),
				  PARTITION p2026 VALUES LESS THAN ('2027-01-01'),
				  PARTITION pmax VALUES LESS THAN (MAXVALUE)
				)
				""");

		jdbcTemplate.execute("""
				CREATE TABLE strain_readings (
				  message_id VARCHAR(64) NOT NULL,
				  project_id VARCHAR(64) NOT NULL,
				  sensor_id VARCHAR(64) NOT NULL,
				  timestamp DATETIME(3) NOT NULL,
				  received_at DATETIME(3) NOT NULL,
				  strain_microstrain DOUBLE NULL,
				  temperature_celsius DOUBLE NULL,
				  PRIMARY KEY (message_id, timestamp),
				  KEY idx_strain_sensor_time (sensor_id, timestamp),
				  KEY idx_strain_project_time (project_id, timestamp)
				) ENGINE=InnoDB
				PARTITION BY RANGE COLUMNS(timestamp) (
				  PARTITION p2025 VALUES LESS THAN ('2026-01-01'),
				  PARTITION p2026 VALUES LESS THAN ('2027-01-01'),
				  PARTITION pmax VALUES LESS THAN (MAXVALUE)
				)
				""");

		jdbcTemplate.execute("""
				CREATE TABLE soil_pressure_readings (
				  message_id VARCHAR(64) NOT NULL,
				  project_id VARCHAR(64) NOT NULL,
				  sensor_id VARCHAR(64) NOT NULL,
				  timestamp DATETIME(3) NOT NULL,
				  received_at DATETIME(3) NOT NULL,
				  pressure_kpa DOUBLE NULL,
				  temperature_celsius DOUBLE NULL,
				  PRIMARY KEY (message_id, timestamp),
				  KEY idx_soilp_sensor_time (sensor_id, timestamp),
				  KEY idx_soilp_project_time (project_id, timestamp)
				) ENGINE=InnoDB
				PARTITION BY RANGE COLUMNS(timestamp) (
				  PARTITION p2025 VALUES LESS THAN ('2026-01-01'),
				  PARTITION p2026 VALUES LESS THAN ('2027-01-01'),
				  PARTITION pmax VALUES LESS THAN (MAXVALUE)
				)
				""");

		jdbcTemplate.execute("""
				CREATE TABLE deep_displacement_readings (
				  message_id VARCHAR(64) NOT NULL,
				  project_id VARCHAR(64) NOT NULL,
				  sensor_id VARCHAR(64) NOT NULL,
				  timestamp DATETIME(3) NOT NULL,
				  received_at DATETIME(3) NOT NULL,
				  depth_m DOUBLE NULL,
				  tilt_x_mm DOUBLE NULL,
				  tilt_y_mm DOUBLE NULL,
				  cumulative_tilt_mm DOUBLE NULL,
				  PRIMARY KEY (message_id, timestamp),
				  KEY idx_deep_sensor_time (sensor_id, timestamp),
				  KEY idx_deep_project_time (project_id, timestamp)
				) ENGINE=InnoDB
				PARTITION BY RANGE COLUMNS(timestamp) (
				  PARTITION p2025 VALUES LESS THAN ('2026-01-01'),
				  PARTITION p2026 VALUES LESS THAN ('2027-01-01'),
				  PARTITION pmax VALUES LESS THAN (MAXVALUE)
				)
				""");
	}

	@Test
	void contextLoads() {
	}

	@Test
	void paginacionYTotalPorSensor() throws Exception {
		jdbcTemplate.execute("TRUNCATE TABLE gnss_readings");

		String projectId = "proj-1";
		String sensorId = "sensor-gnss-1";
		Instant base = Instant.parse("2026-05-01T00:00:00Z");
		for (int i = 0; i < 30; i++) {
			insertGnss(projectId, sensorId, base.plus(Duration.ofHours(i)), base.plus(Duration.ofHours(i)), "4");
		}

		mockMvc.perform(get("/api/v1/sensors/{sensor_id}/readings", sensorId)
						.param("sensor_type", "gnss")
						.param("from", "2026-05-01T00:00:00Z")
						.param("to", "2026-05-03T00:00:00Z")
						.param("offset", "0")
						.param("limit", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(30))
				.andExpect(jsonPath("$.offset").value(0))
				.andExpect(jsonPath("$.limit").value(10))
				.andExpect(jsonPath("$.items.length()").value(10))
				.andExpect(jsonPath("$.items[0].sensorType").value("gnss"));

		mockMvc.perform(get("/api/v1/sensors/{sensor_id}/readings", sensorId)
						.param("sensor_type", "gnss")
						.param("from", "2026-05-01T00:00:00Z")
						.param("to", "2026-05-03T00:00:00Z")
						.param("offset", "10")
						.param("limit", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(30))
				.andExpect(jsonPath("$.items.length()").value(10));
	}

	@Test
	void filtroFixQualitySoloParaGnss() throws Exception {
		jdbcTemplate.execute("TRUNCATE TABLE gnss_readings");
		jdbcTemplate.execute("TRUNCATE TABLE rainfall_readings");

		String projectId = "proj-1";
		String sensorId = "sensor-gnss-1";
		Instant t = Instant.parse("2026-05-01T00:00:00Z");
		insertGnss(projectId, sensorId, t, t, "4");
		insertGnss(projectId, sensorId, t.plusSeconds(3600), t.plusSeconds(3600), "1");

		mockMvc.perform(get("/api/v1/sensors/{sensor_id}/readings", sensorId)
						.param("sensor_type", "gnss")
						.param("from", "2026-05-01T00:00:00Z")
						.param("to", "2026-05-02T00:00:00Z")
						.param("fix_quality", "4")
						.param("offset", "0")
						.param("limit", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.items[0].payload.fix_quality").value("4"));

		mockMvc.perform(get("/api/v1/projects/{project_id}/readings", projectId)
						.param("sensor_type", "rainfall")
						.param("from", "2026-05-01T00:00:00Z")
						.param("to", "2026-05-02T00:00:00Z")
						.param("fix_quality", "4"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rangoDeTiempoCruzandoParticionesAnuales() throws Exception {
		jdbcTemplate.execute("TRUNCATE TABLE gnss_readings");

		String projectId = "proj-1";
		String sensorId = "sensor-gnss-1";
		insertGnss(projectId, sensorId, Instant.parse("2025-12-31T23:00:00Z"), Instant.parse("2025-12-31T23:00:00Z"), "4");
		insertGnss(projectId, sensorId, Instant.parse("2026-01-01T01:00:00Z"), Instant.parse("2026-01-01T01:00:00Z"), "4");

		mockMvc.perform(get("/api/v1/sensors/{sensor_id}/readings", sensorId)
						.param("sensor_type", "gnss")
						.param("from", "2025-12-31T00:00:00Z")
						.param("to", "2026-01-02T00:00:00Z")
						.param("offset", "0")
						.param("limit", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(2));
	}

	@Test
	void performanceConsultaGnssUnMesConTablaGrande() throws Exception {
		jdbcTemplate.execute("TRUNCATE TABLE gnss_readings");

		String projectId = "proj-perf";
		String targetSensorId = "gnss-target";
		String noiseSensorId = "gnss-noise";

		Instant monthStart = Instant.parse("2026-05-01T00:00:00Z");
		for (int i = 0; i < 720; i++) {
			insertGnss(projectId, targetSensorId, monthStart.plus(Duration.ofHours(i)), monthStart.plus(Duration.ofHours(i)), "4");
		}

		LocalDateTime noiseStart = LocalDateTime.of(2025, 1, 1, 0, 0);
		int noiseRows = 100_000 - 720;
		jdbcTemplate.batchUpdate("""
				INSERT INTO gnss_readings (
				  message_id, project_id, sensor_id, timestamp, received_at,
				  displacement_x_mm, displacement_y_mm, displacement_z_mm,
				  displacement_horizontal_mm, cumulative_displacement_mm, fix_quality
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""", new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
				LocalDateTime ts = noiseStart.plusMinutes(i * 10L);
				ps.setString(1, "noise-" + i);
				ps.setString(2, projectId);
				ps.setString(3, noiseSensorId);
				ps.setTimestamp(4, Timestamp.valueOf(ts));
				ps.setTimestamp(5, Timestamp.valueOf(ts));
				ps.setDouble(6, 0.1);
				ps.setDouble(7, 0.2);
				ps.setDouble(8, 0.3);
				ps.setDouble(9, 0.4);
				ps.setDouble(10, 1.0);
				ps.setString(11, "4");
			}

			@Override
			public int getBatchSize() {
				return noiseRows;
			}
		});

		var request = get("/api/v1/sensors/{sensor_id}/readings", targetSensorId)
				.param("sensor_type", "gnss")
				.param("from", "2026-05-01T00:00:00Z")
				.param("to", "2026-06-01T00:00:00Z")
				.param("offset", "0")
				.param("limit", "1000");

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(720));

		LocalDateTime fromUtc = LocalDateTime.ofInstant(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
		LocalDateTime toUtc = LocalDateTime.ofInstant(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

		readingsMapper.selectSensorReadingsPage(targetSensorId, fromUtc, toUtc, "gnss", "4", 0, 1000);

		long bestMs = Long.MAX_VALUE;
		for (int i = 0; i < 10; i++) {
			long startNs = System.nanoTime();
			readingsMapper.selectSensorReadingsPage(targetSensorId, fromUtc, toUtc, "gnss", "4", 0, 1000);
			long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
			bestMs = Math.min(bestMs, elapsedMs);
		}

		assertThat(bestMs).isLessThan(300);
	}

	@Test
	void performanceUltimasLecturasProyectoShangtan() throws Exception {
		jdbcTemplate.execute("TRUNCATE TABLE gnss_readings");
		jdbcTemplate.execute("TRUNCATE TABLE rainfall_readings");
		jdbcTemplate.execute("TRUNCATE TABLE strain_readings");
		jdbcTemplate.execute("TRUNCATE TABLE soil_pressure_readings");
		jdbcTemplate.execute("TRUNCATE TABLE deep_displacement_readings");

		String projectId = "Shangtan";
		Instant now = Instant.parse("2026-05-15T12:00:00Z");

		for (int i = 0; i < 3; i++) {
			insertGnss(projectId, "gnss-" + i, now.minusSeconds(i * 60L), now.minusSeconds(i * 60L), "4");
		}
		insertRainfall(projectId, "rain-1", now.minusSeconds(10), now.minusSeconds(10));
		insertRainfall(projectId, "rain-2", now.minusSeconds(20), now.minusSeconds(20));
		insertStrain(projectId, "strain-1", now.minusSeconds(30), now.minusSeconds(30));
		insertSoilPressure(projectId, "soil-1", now.minusSeconds(40), now.minusSeconds(40));
		insertDeepDisplacement(projectId, "deep-1", now.minusSeconds(50), now.minusSeconds(50));
		insertDeepDisplacement(projectId, "deep-2", now.minusSeconds(55), now.minusSeconds(55));

		var request = get("/api/v1/projects/{project_id}/latest", projectId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(9));

		readingsMapper.selectLatestByProject(projectId, null, null, null, null);

		long bestMs = Long.MAX_VALUE;
		for (int i = 0; i < 10; i++) {
			long startNs = System.nanoTime();
			readingsMapper.selectLatestByProject(projectId, null, null, null, null);
			long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
			bestMs = Math.min(bestMs, elapsedMs);
		}

		assertThat(bestMs).isLessThan(200);
	}

	private void insertGnss(String projectId, String sensorId, Instant timestamp, Instant receivedAt, String fixQuality) {
		jdbcTemplate.update("""
				INSERT INTO gnss_readings (
				  message_id, project_id, sensor_id, timestamp, received_at,
				  displacement_x_mm, displacement_y_mm, displacement_z_mm,
				  displacement_horizontal_mm, cumulative_displacement_mm, fix_quality
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""",
				UUID.randomUUID().toString(),
				projectId,
				sensorId,
				Timestamp.valueOf(LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC)),
				Timestamp.valueOf(LocalDateTime.ofInstant(receivedAt, ZoneOffset.UTC)),
				0.1, 0.2, 0.3, 0.4, 1.0,
				fixQuality
		);
	}

	private void insertRainfall(String projectId, String sensorId, Instant timestamp, Instant receivedAt) {
		jdbcTemplate.update("""
				INSERT INTO rainfall_readings (
				  message_id, project_id, sensor_id, timestamp, received_at,
				  rainfall_mm, cumulative_rainfall_mm, interval_minutes
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""",
				UUID.randomUUID().toString(),
				projectId,
				sensorId,
				Timestamp.valueOf(LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC)),
				Timestamp.valueOf(LocalDateTime.ofInstant(receivedAt, ZoneOffset.UTC)),
				1.0, 10.0, 60
		);
	}

	private void insertStrain(String projectId, String sensorId, Instant timestamp, Instant receivedAt) {
		jdbcTemplate.update("""
				INSERT INTO strain_readings (
				  message_id, project_id, sensor_id, timestamp, received_at,
				  strain_microstrain, temperature_celsius
				) VALUES (?, ?, ?, ?, ?, ?, ?)
				""",
				UUID.randomUUID().toString(),
				projectId,
				sensorId,
				Timestamp.valueOf(LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC)),
				Timestamp.valueOf(LocalDateTime.ofInstant(receivedAt, ZoneOffset.UTC)),
				123.0, 20.0
		);
	}

	private void insertSoilPressure(String projectId, String sensorId, Instant timestamp, Instant receivedAt) {
		jdbcTemplate.update("""
				INSERT INTO soil_pressure_readings (
				  message_id, project_id, sensor_id, timestamp, received_at,
				  pressure_kpa, temperature_celsius
				) VALUES (?, ?, ?, ?, ?, ?, ?)
				""",
				UUID.randomUUID().toString(),
				projectId,
				sensorId,
				Timestamp.valueOf(LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC)),
				Timestamp.valueOf(LocalDateTime.ofInstant(receivedAt, ZoneOffset.UTC)),
				50.0, 20.0
		);
	}

	private void insertDeepDisplacement(String projectId, String sensorId, Instant timestamp, Instant receivedAt) {
		jdbcTemplate.update("""
				INSERT INTO deep_displacement_readings (
				  message_id, project_id, sensor_id, timestamp, received_at,
				  depth_m, tilt_x_mm, tilt_y_mm, cumulative_tilt_mm
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				""",
				UUID.randomUUID().toString(),
				projectId,
				sensorId,
				Timestamp.valueOf(LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC)),
				Timestamp.valueOf(LocalDateTime.ofInstant(receivedAt, ZoneOffset.UTC)),
				1.0, 0.1, 0.2, 0.3
		);
	}

}
