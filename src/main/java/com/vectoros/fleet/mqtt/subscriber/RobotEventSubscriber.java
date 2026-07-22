package com.vectoros.fleet.mqtt.subscriber;

import com.vectoros.fleet.mqtt.config.MqttClientGateway;
import com.vectoros.fleet.mqtt.events.RobotStatusEvent;
import com.vectoros.fleet.mqtt.events.RobotTelemetryEvent;
import com.vectoros.fleet.mqtt.events.TaskCompletedEvent;
import com.vectoros.fleet.mqtt.serialization.MqttEventSerializer;
import com.vectoros.fleet.mqtt.serialization.MqttSerializationException;
import com.vectoros.fleet.mqtt.topics.MqttTopics;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Subscribes to robot-originated MQTT topics.
 * <p>
 * Sprint 03 logs received events. Persistence is deferred to future sprints.
 */
@Component
@ConditionalOnBean(MqttClientGateway.class)
public class RobotEventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(RobotEventSubscriber.class);

    private final MqttClientGateway mqttClientGateway;
    private final MqttEventSerializer serializer;

    public RobotEventSubscriber(MqttClientGateway mqttClientGateway,
                                MqttEventSerializer serializer) {
        this.mqttClientGateway = mqttClientGateway;
        this.serializer = serializer;
    }

    @PostConstruct
    public void subscribeAll() {
        mqttClientGateway.subscribe(MqttTopics.ROBOTS_STATUS, this::onRobotStatus);
        mqttClientGateway.subscribe(MqttTopics.ROBOTS_TELEMETRY, this::onRobotTelemetry);
        mqttClientGateway.subscribe(MqttTopics.TASKS_COMPLETED, this::onTaskCompleted);
    }

    private void onRobotStatus(String topic, MqttMessage message) {
        try {
            RobotStatusEvent event = serializer.deserialize(message.getPayload(), RobotStatusEvent.class);
            log.info("MQTT event received: topic={} robotId={} status={} battery={}",
                    topic, event.getRobotId(), event.getStatus(), event.getBatteryLevel());
        } catch (MqttSerializationException ex) {
            log.warn("Unexpected MQTT message on {}: {}", topic, ex.getMessage());
        }
    }

    private void onRobotTelemetry(String topic, MqttMessage message) {
        try {
            RobotTelemetryEvent event = serializer.deserialize(message.getPayload(), RobotTelemetryEvent.class);
            log.info("MQTT event received: topic={} robotId={} battery={} x={} y={} speed={}",
                    topic,
                    event.getRobotId(),
                    event.getBatteryLevel(),
                    event.getCurrentX(),
                    event.getCurrentY(),
                    event.getSpeed());
        } catch (MqttSerializationException ex) {
            log.warn("Unexpected MQTT message on {}: {}", topic, ex.getMessage());
        }
    }

    private void onTaskCompleted(String topic, MqttMessage message) {
        try {
            TaskCompletedEvent event = serializer.deserialize(message.getPayload(), TaskCompletedEvent.class);
            log.info("MQTT event received: topic={} taskId={} robotId={} completedAt={}",
                    topic, event.getTaskId(), event.getRobotId(), event.getCompletedAt());
        } catch (MqttSerializationException ex) {
            log.warn("Unexpected MQTT message on {}: {}", topic, ex.getMessage());
        }
    }
}
