package com.vectoros.fleet.mqtt.publisher;

import com.vectoros.fleet.mqtt.config.MqttClientGateway;
import com.vectoros.fleet.mqtt.events.TaskAssignedEvent;
import com.vectoros.fleet.mqtt.events.TaskCancelledEvent;
import com.vectoros.fleet.mqtt.events.TaskStatusUpdatedEvent;
import com.vectoros.fleet.mqtt.serialization.MqttEventSerializer;
import com.vectoros.fleet.mqtt.serialization.MqttSerializationException;
import com.vectoros.fleet.mqtt.topics.MqttTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Publishes robot command events to MQTT.
 * <p>
 * Business services depend on this class rather than MQTT client details.
 * When MQTT is disabled, publishes are skipped safely.
 */
@Component
public class RobotCommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(RobotCommandPublisher.class);

    private final ObjectProvider<MqttClientGateway> mqttClientGateway;
    private final MqttEventSerializer serializer;

    public RobotCommandPublisher(ObjectProvider<MqttClientGateway> mqttClientGateway,
                                 MqttEventSerializer serializer) {
        this.mqttClientGateway = mqttClientGateway;
        this.serializer = serializer;
    }

    public void publishTaskAssigned(TaskAssignedEvent event) {
        publish(MqttTopics.TASKS_ASSIGNED, event);
    }

    public void publishTaskStatusUpdated(TaskStatusUpdatedEvent event) {
        publish(MqttTopics.TASKS_STATUS, event);
    }

    public void publishTaskCancelled(TaskCancelledEvent event) {
        publish(MqttTopics.TASKS_CANCELLED, event);
    }

    private void publish(String topic, Object event) {
        MqttClientGateway gateway = mqttClientGateway.getIfAvailable();
        if (gateway == null) {
            log.debug("MQTT disabled — skipping publish: topic={} event={}",
                    topic, event.getClass().getSimpleName());
            return;
        }

        try {
            byte[] payload = serializer.serialize(event);
            gateway.publish(topic, payload);
        } catch (MqttSerializationException ex) {
            log.error("MQTT serialization failure: topic={} event={} reason={}",
                    topic, event.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
