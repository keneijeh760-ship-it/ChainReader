package com.vectoros.fleet.mqtt.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectoros.fleet.entity.RobotStatus;
import com.vectoros.fleet.mqtt.config.MqttClientGateway;
import com.vectoros.fleet.mqtt.events.RobotStatusEvent;
import com.vectoros.fleet.mqtt.events.RobotTelemetryEvent;
import com.vectoros.fleet.mqtt.events.TaskCompletedEvent;
import com.vectoros.fleet.mqtt.serialization.MqttEventSerializer;
import com.vectoros.fleet.mqtt.topics.MqttTopics;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RobotEventSubscriberTest {

    @Mock
    MqttClientGateway gateway;

    MqttEventSerializer serializer;
    RobotEventSubscriber subscriber;

    @BeforeEach
    void setUp() {
        serializer = new MqttEventSerializer(new ObjectMapper().findAndRegisterModules());
        subscriber = new RobotEventSubscriber(gateway, serializer);
    }

    @Test
    void subscribeAll_registersExpectedTopics() {
        subscriber.subscribeAll();

        verify(gateway).subscribe(eq(MqttTopics.ROBOTS_STATUS), org.mockito.ArgumentMatchers.any());
        verify(gateway).subscribe(eq(MqttTopics.ROBOTS_TELEMETRY), org.mockito.ArgumentMatchers.any());
        verify(gateway).subscribe(eq(MqttTopics.TASKS_COMPLETED), org.mockito.ArgumentMatchers.any());
        verify(gateway, times(3)).subscribe(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void robotStatusListener_handlesValidPayload() throws Exception {
        subscriber.subscribeAll();

        ArgumentCaptor<IMqttMessageListener> listenerCaptor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(gateway).subscribe(eq(MqttTopics.ROBOTS_STATUS), listenerCaptor.capture());

        RobotStatusEvent event = RobotStatusEvent.builder()
                .robotId(1L)
                .status(RobotStatus.IDLE)
                .batteryLevel(100)
                .timestamp(Instant.now())
                .build();

        MqttMessage message = new MqttMessage(serializer.serialize(event));
        assertDoesNotThrow(() -> listenerCaptor.getValue().messageArrived(MqttTopics.ROBOTS_STATUS, message));
    }

    @Test
    void telemetryAndCompletedListeners_handleValidPayloads() throws Exception {
        subscriber.subscribeAll();

        ArgumentCaptor<IMqttMessageListener> telemetryCaptor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        ArgumentCaptor<IMqttMessageListener> completedCaptor = ArgumentCaptor.forClass(IMqttMessageListener.class);

        verify(gateway).subscribe(eq(MqttTopics.ROBOTS_TELEMETRY), telemetryCaptor.capture());
        verify(gateway).subscribe(eq(MqttTopics.TASKS_COMPLETED), completedCaptor.capture());

        RobotTelemetryEvent telemetry = RobotTelemetryEvent.builder()
                .robotId(1L)
                .batteryLevel(90)
                .currentX(1.0)
                .currentY(2.0)
                .speed(0.5)
                .heading(45.0)
                .timestamp(Instant.now())
                .build();

        TaskCompletedEvent completed = TaskCompletedEvent.builder()
                .taskId(10L)
                .robotId(1L)
                .completedAt(Instant.now())
                .build();

        assertDoesNotThrow(() -> telemetryCaptor.getValue()
                .messageArrived(MqttTopics.ROBOTS_TELEMETRY, new MqttMessage(serializer.serialize(telemetry))));
        assertDoesNotThrow(() -> completedCaptor.getValue()
                .messageArrived(MqttTopics.TASKS_COMPLETED, new MqttMessage(serializer.serialize(completed))));
    }

    @Test
    void robotStatusListener_handlesInvalidPayloadWithoutThrowing() throws Exception {
        subscriber.subscribeAll();

        ArgumentCaptor<IMqttMessageListener> listenerCaptor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(gateway).subscribe(eq(MqttTopics.ROBOTS_STATUS), listenerCaptor.capture());

        assertDoesNotThrow(() -> listenerCaptor.getValue()
                .messageArrived(MqttTopics.ROBOTS_STATUS, new MqttMessage("bad-json".getBytes())));
    }
}
