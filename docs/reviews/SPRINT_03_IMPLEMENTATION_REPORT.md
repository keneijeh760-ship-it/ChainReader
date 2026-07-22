# Sprint 03 — Implementation Report
## Robot Communication Infrastructure (MQTT)

Version: 1.0  
Status: Complete  
Date: 2026-07-22

---

# Summary

Sprint 03 establishes production-quality MQTT communication between Fleet Service and Robot Service using Eclipse Mosquitto and the Eclipse Paho client.

Fleet can publish task command events and subscribe to robot status, telemetry, and task-completed events. Persistence of inbound events is deferred to future sprints.

---

# Architectural Decisions

## 1. Eclipse Paho (not Spring Integration MQTT)

A thin custom gateway (`MqttClientGateway`) wraps Paho so business code never depends on MQTT APIs directly. This matches the sprint package layout (`config`, `publisher`, `subscriber`, `events`, `topics`, `serialization`) and keeps the stack explicit.

## 2. Topic constants only

All topics live in `MqttTopics`. Hardcoded topic strings are prohibited in publishers and subscribers.

## 3. Services publish through `RobotCommandPublisher`

Controllers never publish MQTT messages. `TaskServiceImpl` publishes domain events after assignment, status updates, and cancellation.

## 4. Resilient connection

Broker connection failures are logged and do not crash the application. Publishes/subscribes attempt reconnect and fail safely with error logs.

## 5. MQTT can be disabled

`vectoros.mqtt.enabled=false` skips MQTT beans. Tests disable MQTT by default; `MqttIntegrationTest` enables a Mosquitto Testcontainer when Docker is available.

---

# Package Structure

```
mqtt/
├── config/          MqttProperties, MqttConfiguration, MqttClientGateway
├── events/          TaskAssignedEvent, TaskCancelledEvent, TaskStatusUpdatedEvent,
│                    TaskCompletedEvent, RobotStatusEvent, RobotTelemetryEvent
├── publisher/       RobotCommandPublisher
├── serialization/   MqttEventSerializer
├── subscriber/      RobotEventSubscriber
└── topics/          MqttTopics
```

---

# Topics

| Constant | Topic |
|----------|-------|
| `TASKS_ASSIGNED` | `warehouse/tasks/assigned` |
| `TASKS_CANCELLED` | `warehouse/tasks/cancelled` |
| `TASKS_STATUS` | `warehouse/tasks/status` |
| `TASKS_COMPLETED` | `warehouse/tasks/completed` |
| `ROBOTS_STATUS` | `warehouse/robots/status` |
| `ROBOTS_TELEMETRY` | `warehouse/robots/telemetry` |
| `SYSTEM_HEARTBEAT` | `warehouse/system/heartbeat` |

---

# Docker

`docker-compose.yml` now includes:

- PostgreSQL
- Eclipse Mosquitto (`1883`, `9001`)
- Fleet Service (`MQTT_BROKER_URL=tcp://mosquitto:1883`)

Config: `mosquitto/config/mosquitto.conf`

---

# Test Results

```
mvn clean test

Tests run: 50
Failures:  0
Errors:    0
Skipped:   7  (Testcontainers — Docker not available)

BUILD SUCCESS
```

MQTT unit coverage:

- `MqttEventSerializerTest` (5)
- `RobotCommandPublisherTest` (4)
- `RobotEventSubscriberTest` (4)
- `MqttTopicsTest` (1)
- `MqttIntegrationTest` (2 — skipped without Docker)

---

# Remaining TODOs

| Item | Sprint |
|------|--------|
| Persist robot status / telemetry from MQTT | Future |
| Apply task-completed events to task state | Future |
| Robot simulator publishing telemetry | Future |
| System heartbeat publisher | Future |
| MQTT authentication (username/password in production) | Future |

---

# Out of Scope (confirmed)

Robot simulator, telemetry persistence, Dashboard, Kafka, scheduling, firmware.
