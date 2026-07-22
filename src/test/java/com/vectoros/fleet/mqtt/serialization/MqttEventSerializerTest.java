package com.vectoros.fleet.mqtt.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vectoros.fleet.entity.Priority;
import com.vectoros.fleet.entity.RobotStatus;
import com.vectoros.fleet.entity.TaskStatus;
import com.vectoros.fleet.mqtt.events.RobotStatusEvent;
import com.vectoros.fleet.mqtt.events.RobotTelemetryEvent;
import com.vectoros.fleet.mqtt.events.TaskAssignedEvent;
import com.vectoros.fleet.mqtt.events.TaskCompletedEvent;
import com.vectoros.fleet.mqtt.events.TaskStatusUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MqttEventSerializerTest {

    private MqttEventSerializer serializer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        serializer = new MqttEventSerializer(objectMapper);
    }

    @Test
    void serializeAndDeserialize_taskAssignedEvent() {
        TaskAssignedEvent original = TaskAssignedEvent.builder()
                .taskId(1L)
                .taskNumber("TASK-000001")
                .robotId(3L)
                .pickupLocation("A12")
                .dropoffLocation("C18")
                .priority(Priority.HIGH)
                .timestamp(Instant.parse("2026-07-22T10:00:00Z"))
                .build();

        byte[] payload = serializer.serialize(original);
        TaskAssignedEvent restored = serializer.deserialize(payload, TaskAssignedEvent.class);

        assertEquals(original, restored);
    }

    @Test
    void serializeAndDeserialize_taskStatusUpdatedEvent() {
        TaskStatusUpdatedEvent original = TaskStatusUpdatedEvent.builder()
                .taskId(1L)
                .status(TaskStatus.IN_PROGRESS)
                .timestamp(Instant.parse("2026-07-22T10:00:00Z"))
                .build();

        TaskStatusUpdatedEvent restored = serializer.deserialize(
                serializer.serialize(original), TaskStatusUpdatedEvent.class);

        assertEquals(original, restored);
    }

    @Test
    void serializeAndDeserialize_robotStatusAndTelemetry() {
        RobotStatusEvent status = RobotStatusEvent.builder()
                .robotId(2L)
                .status(RobotStatus.WORKING)
                .batteryLevel(88)
                .timestamp(Instant.parse("2026-07-22T10:00:00Z"))
                .build();

        RobotTelemetryEvent telemetry = RobotTelemetryEvent.builder()
                .robotId(2L)
                .batteryLevel(88)
                .currentX(1.5)
                .currentY(2.5)
                .speed(0.8)
                .heading(90.0)
                .timestamp(Instant.parse("2026-07-22T10:00:00Z"))
                .build();

        assertEquals(status, serializer.deserialize(serializer.serialize(status), RobotStatusEvent.class));
        assertEquals(telemetry, serializer.deserialize(serializer.serialize(telemetry), RobotTelemetryEvent.class));
    }

    @Test
    void serializeAndDeserialize_taskCompletedEvent() {
        TaskCompletedEvent original = TaskCompletedEvent.builder()
                .taskId(1L)
                .robotId(3L)
                .completedAt(Instant.parse("2026-07-22T10:00:00Z"))
                .build();

        assertEquals(original, serializer.deserialize(serializer.serialize(original), TaskCompletedEvent.class));
    }

    @Test
    void deserialize_throws_whenPayloadIsInvalid() {
        assertThrows(MqttSerializationException.class,
                () -> serializer.deserialize("not-json".getBytes(), TaskAssignedEvent.class));
    }
}
