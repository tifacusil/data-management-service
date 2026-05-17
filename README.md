# Data Management Service (Java)

Subscriber + persistence microservice for the landslide monitoring system. Consumes JSON envelopes from RabbitMQ, routes by `sensor_type`, persists readings to MySQL (via MyBatis), exposes a REST API for querying stored readings, and (optionally) registers itself in Consul with an HTTP health check.

---

## Table of contents

1. [Local development](#local-development)
2. [Configuration](#configuration)
3. [Infrastructure](#infrastructure)
4. [RabbitMQ topology](#rabbitmq-topology)
5. [MySQL persistence](#mysql-persistence)
6. [Consul service registration](#consul-service-registration)
7. [Deploying to Linux (Ubuntu 24.04)](#deploying-to-linux-ubuntu-2404)
8. [Managing the service](#managing-the-service)
9. [Running multiple instances](#running-multiple-instances)
10. [Publishing a test message](#publishing-a-test-message)
11. [Architecture](#architecture)
12. [Sensor types](#sensor-types)
13. [Dead letter behavior](#dead-letter-behavior)

---

## Local development

**Requirements:** Java 17.

```bash
# Build
./mvnw -q -DskipTests=false clean package

# Test
./mvnw -q test

# Run
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd -q -DskipTests=false clean package
mvnw.cmd -q test
mvnw.cmd spring-boot:run
```

Default HTTP port is `8082` (see `server.port` in `src/main/resources/application.yml`).

---

## Configuration

All configuration lives in `src/main/resources/application.yml` and can be overridden via environment variables (Spring Boot standard mapping rules apply).

### Core

| Setting | Default | Env var equivalent |
|---|---:|---|
| `server.port` | `8082` | `SERVER_PORT` |

### RabbitMQ

| Setting | Default | Env var equivalent |
|---|---:|---|
| `spring.rabbitmq.host` | `204.168.203.32` | `SPRING_RABBITMQ_HOST` |
| `spring.rabbitmq.port` | `5672` | `SPRING_RABBITMQ_PORT` |
| `spring.rabbitmq.username` | `subscriber` | `SPRING_RABBITMQ_USERNAME` |
| `spring.rabbitmq.password` | `${RABBITMQ_PASSWORD:}` | `RABBITMQ_PASSWORD` or `SPRING_RABBITMQ_PASSWORD` |
| `app.rabbitmq.listeners.enabled` | `true` | `APP_RABBITMQ_LISTENERS_ENABLED` |

Listener is configured as **manual ack**.

### MySQL

| Setting | Default | Env var equivalent |
|---|---:|---|
| `spring.datasource.url` | `jdbc:mysql://204.168.203.32:3306/landslide_monitoring?...` | `SPRING_DATASOURCE_URL` |
| `spring.datasource.username` | `landslide` | `SPRING_DATASOURCE_USERNAME` |
| `spring.datasource.password` | `${MYSQL_PASSWORD:}` | `MYSQL_PASSWORD` or `SPRING_DATASOURCE_PASSWORD` |

### Consul (optional)

| Setting | Default | Env var equivalent |
|---|---:|---|
| `app.consul.enabled` | `true` | `CONSUL_ENABLED` or `APP_CONSUL_ENABLED` |
| `app.consul.base-url` | `http://localhost:8500` | `CONSUL_BASE_URL` or `APP_CONSUL_BASE_URL` |
| `app.consul.token` | `${CONSUL_HTTP_TOKEN:}` | `CONSUL_HTTP_TOKEN` or `APP_CONSUL_TOKEN` |
| `app.consul.service-name` | `data-management` | `APP_CONSUL_SERVICE_NAME` |
| `app.consul.service-ip` | `${SERVICE_IP:}` | `SERVICE_IP` or `APP_CONSUL_SERVICE_IP` |
| `app.consul.health-path` | `/health` | `APP_CONSUL_HEALTH_PATH` |
| `app.consul.interval` | `10s` | `APP_CONSUL_INTERVAL` |
| `app.consul.heartbeat-ms` | `10000` | `APP_CONSUL_HEARTBEAT_MS` |
| `app.consul.deregister-after` | `30s` | `APP_CONSUL_DEREGISTER_AFTER` |

**Secret rule:** never commit `RABBITMQ_PASSWORD`, `MYSQL_PASSWORD`, or `CONSUL_HTTP_TOKEN` to git.

---

## Infrastructure

This service depends on:

| Service | Notes |
|---|---|
| RabbitMQ | Consumes from queue `data_management.persist` |
| MySQL | Persists readings into per-sensor tables |
| Consul (optional) | Service discovery + health check registration |

Verify connectivity before running:

```bash
# RabbitMQ AMQP
nc -z -w3 <rabbitmq-host> 5672 && echo "RabbitMQ OK"

# MySQL
nc -z -w3 <mysql-host> 3306 && echo "MySQL OK"

# Consul (optional)
curl -s http://<consul-host>:8500/v1/agent/self | python3 -m json.tool | grep "\"Version\""
```

---

## RabbitMQ topology

This service **only consumes** — it does not declare exchanges, queues, or bindings.

| Element | Value |
|---|---|
| Queue | `data_management.persist` |
| Ack mode | manual |

Message handling behavior:

- Valid JSON + supported `sensor_type` + MySQL write ok → `basicAck`
- Invalid JSON or unsupported `sensor_type` → `basicNack(requeue=false)`
- Transient MySQL failure (connection/timeout/transient SQL) → `basicNack(requeue=true)`

---

## MySQL persistence

Each envelope is persisted into a sensor-specific table using `INSERT IGNORE` keyed by `message_id` to avoid duplicates on retries.

| `sensor_type` | Table |
|---|---|
| `gnss` | `gnss_readings` |
| `rainfall` | `rainfall_readings` |
| `strain` | `strain_readings` |
| `soil_pressure` | `soil_pressure_readings` |
| `deep_displacement` | `deep_displacement_readings` |

All timestamps are stored in UTC (the service converts incoming `Instant` to `LocalDateTime` at `ZoneOffset.UTC`).

---

## Consul service registration

When enabled (`app.consul.enabled=true`), the service registers itself in Consul on startup and refreshes registration periodically:

- Register: `PUT /v1/agent/service/register`
- Heartbeat: re-register every `app.consul.heartbeat-ms`
- Deregister: `PUT /v1/agent/service/deregister/{serviceId}` on shutdown

Registration payload uses:

- **Name:** `app.consul.service-name` (default `data-management`)
- **Address:** `app.consul.service-ip` if set; otherwise auto-detected IPv4
- **Port:** `server.port`
- **Health check:** `http://{ip}:{port}{app.consul.health-path}`

Verify registration:

```bash
curl -s http://<consul-host>:8500/v1/catalog/service/data-management | python3 -m json.tool
curl -s http://<consul-host>:8500/v1/health/service/data-management | python3 -m json.tool
curl -sv http://<service-host>:8082/health
```

---

## Deploying to Linux (Ubuntu 24.04)

Build artifact:

```bash
./mvnw -q -DskipTests=false clean package
ls -lah target/*.jar
```

Run on server:

```bash
export RABBITMQ_PASSWORD="<rabbitmq password>"
export MYSQL_PASSWORD="<mysql password>"
export CONSUL_HTTP_TOKEN="<consul token>"    # optional
export CONSUL_BASE_URL="http://<consul-host>:8500"
export SERVICE_IP="<service ip>"             # recommended when registering to Consul

java -jar target/data-management-service-0.0.1-SNAPSHOT.jar
```

---

## Managing the service

HTTP endpoints:

| Endpoint | Purpose |
|---|---|
| `GET /health` | Health check: RabbitMQ + MySQL + processed count |
| `GET /swagger-ui.html` | Swagger UI |
| `GET /v3/api-docs` | OpenAPI JSON |
| `GET /api/v1/sensors/{sensor_id}/readings` | Readings for a sensor (paged) |
| `GET /api/v1/projects/{project_id}/readings` | Readings for a project (paged) |
| `GET /api/v1/projects/{project_id}/latest` | Latest readings for a project |

Query parameters supported on the readings endpoints:

- `from`, `to` as ISO-8601 timestamps
- `sensor_type` in `gnss|rainfall|strain|soil_pressure|deep_displacement`
- `fix_quality` (only applies when `sensor_type=gnss`)
- `offset` (default `0`), `limit` (default `200`, max `1000`)

---

## Running multiple instances

Normal deployment: multiple instances can run as independent consumers of the same queue (competing consumers). Ensure each instance uses the same RabbitMQ + MySQL configuration.

Consul: if enabled, each instance registers with a unique ID derived from `{service-name}-{ip}-{port}`. Use distinct `SERVICE_IP` and/or `SERVER_PORT` per host as needed.

---

## Publishing a test message

This publishes an example envelope to the queue for local verification. It assumes the queue already exists and is routable.

```bash
rabbitmqadmin publish \
  exchange=amq.default \
  routing_key=data_management.persist \
  payload='{"schema_version":"1","message_id":"550e8400-e29b-41d4-a716-446655440000","project_id":"demo","sensor_id":"S-01","sensor_type":"rainfall","timestamp":"2026-05-17T00:00:00Z","received_at":"2026-05-17T00:00:01Z","sampling_interval_seconds":60,"payload":{"rainfall_mm":1.2,"cumulative_rainfall_mm":10.5,"interval_minutes":60}}'
```

---

## Architecture

```
RabbitMQ queue: data_management.persist
    │
    ▼
DataManagementQueueListener  (manual ack)
    │  parse JSON → Envelope
    │
    ▼
MessageRouter
    ├─ sensor_type=gnss              → persistGnss(Envelope, GnssPayload)              → gnss_readings
    ├─ sensor_type=rainfall          → persistRainfall(Envelope, RainfallPayload)      → rainfall_readings
    ├─ sensor_type=strain            → persistStrain(Envelope, StrainPayload)          → strain_readings
    ├─ sensor_type=soil_pressure     → persistSoilPressure(Envelope, SoilPressurePayload) → soil_pressure_readings
    └─ sensor_type=deep_displacement → persistDeepDisplacement(Envelope, DeepDisplacementPayload) → deep_displacement_readings
    │
    └─ errors:
         - invalid JSON / unsupported type → nack requeue=false
         - transient MySQL error           → nack requeue=true

REST API (Spring MVC)
    - /health
    - /api/v1/...
    - /swagger-ui.html

ConsulAutoRegistrar (optional)
    - registers service with HTTP check on /health
```

---

## Sensor types

| `sensor_type` | Payload model |
|---|---|
| `gnss` | `GnssPayload` |
| `rainfall` | `RainfallPayload` |
| `strain` | `StrainPayload` |
| `soil_pressure` | `SoilPressurePayload` |
| `deep_displacement` | `DeepDisplacementPayload` |

Envelope shape (minimal):

```json
{
  "schema_version": "1",
  "message_id": "uuid",
  "project_id": "project",
  "sensor_id": "sensor",
  "sensor_type": "rainfall",
  "timestamp": "2026-05-17T00:00:00Z",
  "received_at": "2026-05-17T00:00:01Z",
  "sampling_interval_seconds": 60,
  "payload": { }
}
```

---

## Dead letter behavior

This service does not write its own DLX files. The dead-letter behavior is controlled by RabbitMQ queue policy/topology.

- Messages NACKed with `requeue=false` can be routed to a dead-letter exchange if the queue is configured with `x-dead-letter-exchange`.
- Messages NACKed with `requeue=true` stay in the queue and will be retried by RabbitMQ redelivery.

