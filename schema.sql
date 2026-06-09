-- ============================================================
-- Base de datos: data_management_db
-- Servicio: data-management-demo (Spring Boot + MyBatis)
-- Tablas: gnss_readings, rainfall_readings, strain_readings,
--         soil_pressure_readings, deep_displacement_readings
-- ============================================================

CREATE DATABASE IF NOT EXISTS data_management_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE data_management_db;

-- Lecturas GNSS (desplazamiento)
CREATE TABLE IF NOT EXISTS gnss_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64),
    project_id VARCHAR(64) NOT NULL,
    sensor_id VARCHAR(64) NOT NULL,
    timestamp DATETIME NOT NULL,
    received_at DATETIME NOT NULL,
    displacement_x_mm DOUBLE,
    displacement_y_mm DOUBLE,
    displacement_z_mm DOUBLE,
    displacement_horizontal_mm DOUBLE,
    cumulative_displacement_mm DOUBLE,
    fix_quality VARCHAR(16),
    UNIQUE KEY uk_gnss_message (message_id),
    INDEX idx_gnss_sensor_ts (sensor_id, timestamp),
    INDEX idx_gnss_project_ts (project_id, timestamp)
);

-- Lecturas de precipitacion
CREATE TABLE IF NOT EXISTS rainfall_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64),
    project_id VARCHAR(64) NOT NULL,
    sensor_id VARCHAR(64) NOT NULL,
    timestamp DATETIME NOT NULL,
    received_at DATETIME NOT NULL,
    rainfall_mm DOUBLE,
    cumulative_rainfall_mm DOUBLE,
    interval_minutes INT,
    UNIQUE KEY uk_rainfall_message (message_id),
    INDEX idx_rainfall_sensor_ts (sensor_id, timestamp),
    INDEX idx_rainfall_project_ts (project_id, timestamp)
);

-- Lecturas de deformacion (strain)
CREATE TABLE IF NOT EXISTS strain_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64),
    project_id VARCHAR(64) NOT NULL,
    sensor_id VARCHAR(64) NOT NULL,
    timestamp DATETIME NOT NULL,
    received_at DATETIME NOT NULL,
    strain_microstrain DOUBLE,
    temperature_celsius DOUBLE,
    UNIQUE KEY uk_strain_message (message_id),
    INDEX idx_strain_sensor_ts (sensor_id, timestamp),
    INDEX idx_strain_project_ts (project_id, timestamp)
);

-- Lecturas de presion de suelo
CREATE TABLE IF NOT EXISTS soil_pressure_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64),
    project_id VARCHAR(64) NOT NULL,
    sensor_id VARCHAR(64) NOT NULL,
    timestamp DATETIME NOT NULL,
    received_at DATETIME NOT NULL,
    pressure_kpa DOUBLE,
    temperature_celsius DOUBLE,
    UNIQUE KEY uk_soil_pressure_message (message_id),
    INDEX idx_soil_pressure_sensor_ts (sensor_id, timestamp),
    INDEX idx_soil_pressure_project_ts (project_id, timestamp)
);

-- Lecturas de desplazamiento profundo (inclinometro)
CREATE TABLE IF NOT EXISTS deep_displacement_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64),
    project_id VARCHAR(64) NOT NULL,
    sensor_id VARCHAR(64) NOT NULL,
    timestamp DATETIME NOT NULL,
    received_at DATETIME NOT NULL,
    depth_m DOUBLE,
    tilt_x_mm DOUBLE,
    tilt_y_mm DOUBLE,
    cumulative_tilt_mm DOUBLE,
    UNIQUE KEY uk_deep_displacement_message (message_id),
    INDEX idx_deep_displacement_sensor_ts (sensor_id, timestamp),
    INDEX idx_deep_displacement_project_ts (project_id, timestamp)
);
