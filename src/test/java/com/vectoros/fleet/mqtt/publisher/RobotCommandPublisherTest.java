package com.vectoros.fleet.mqtt.publisher;

import com.vectoros.fleet.entity.Priority;
import com.vectoros.fleet.entity.TaskStatus;
import com.vectoros.fleet.mqtt.config.MqttClientGateway;
import com.vectoros.fleet.mqtt.events.TaskAssignedEvent;
import com.vectoros.fleet.mqtt.events.TaskCancelledEvent;
import com.vectoros.fleet.mqtt.events.TaskStatusUpdatedEvent;
import com.vectoros.fleet.mqtt.serialization.MqttEventSerializer;
import com.vectoros.fleet.mqtt.topics.MqttTopics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RobotCommandPublisherTest {

    @Mock
    ObjectProvider<MqttClientGateway> gatewayProvider;

    @Mock
    MqttClientGateway gateway;

    @Mock
    MqttEventSerializer serializer;

    RobotCommandPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RobotCommandPublisher(gatewayProvider, serializer);
    }

    @Test
    void publishTaskAssigned_publishesToAssignedTopic() {
        when(gatewayProvider.getIfAvailable()).thenReturn(gateway);
        TaskAssignedEvent event = TaskAssignedEvent.builder()
                .taskId(1L)
                .taskNumber("TASK-000001")
                .robotId(3L)
                .pickupLocation("A12")
                .dropoffLocation("C18")
                .priority(Priority.HIGH)
                .timestamp(Instant.now())
                .build();
        byte[] payload = "{\"taskId\":1}".getBytes();
        when(serializer.serialize(event)).thenReturn(payload);

        publisher.publishTaskAssigned(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(gateway).publish(topicCaptor.capture(), payloadCaptor.capture());
        assertEquals(MqttTopics.TASKS_ASSIGNED, topicCaptor.getValue());
        assertArrayEquals(payload, payloadCaptor.getValue());
    }

    @Test
    void publishTaskStatusUpdated_publishesToStatusTopic() {
        when(gatewayProvider.getIfAvailable()).thenReturn(gateway);
        TaskStatusUpdatedEvent event = TaskStatusUpdatedEvent.builder()
                .taskId(1L)
                .status(TaskStatus.IN_PROGRESS)
                .timestamp(Instant.now())
                .build();
        when(serializer.serialize(event)).thenReturn("{}".getBytes());

        publisher.publishTaskStatusUpdated(event);

        verify(gateway).publish(MqttTopics.TASKS_STATUS, "{}".getBytes());
    }

    @Test
    void publishTaskCancelled_publishesToCancelledTopic() {
        when(gatewayProvider.getIfAvailable()).thenReturn(gateway);
        TaskCancelledEvent event = TaskCancelledEvent.builder()
                .taskId(1L)
                .taskNumber("TASK-000001")
                .robotId(3L)
                .timestamp(Instant.now())
                .build();
        when(serializer.serialize(event)).thenReturn("{}".getBytes());

        publisher.publishTaskCancelled(event);

        verify(gateway).publish(MqttTopics.TASKS_CANCELLED, "{}".getBytes());
    }

    @Test
    void publish_skipsWhenMqttDisabled() {
        when(gatewayProvider.getIfAvailable()).thenReturn(null);

        publisher.publishTaskAssigned(TaskAssignedEvent.builder()
                .taskId(1L)
                .taskNumber("TASK-000001")
                .robotId(3L)
                .pickupLocation("A12")
                .dropoffLocation("C18")
                .priority(Priority.LOW)
                .timestamp(Instant.now())
                .build());

        verifyNoInteractions(serializer);
        verifyNoInteractions(gateway);
    }
}
