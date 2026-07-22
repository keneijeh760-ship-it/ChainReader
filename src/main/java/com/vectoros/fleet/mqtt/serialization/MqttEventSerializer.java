package com.vectoros.fleet.mqtt.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Serializes and deserializes MQTT payloads using Jackson JSON.
 */
@Component
public class MqttEventSerializer {

    private final ObjectMapper objectMapper;

    public MqttEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] serialize(Object event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (JsonProcessingException ex) {
            throw new MqttSerializationException("Failed to serialize MQTT event: " + event.getClass().getSimpleName(), ex);
        }
    }

    public <T> T deserialize(byte[] payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (IOException ex) {
            String preview = payload == null ? "null" : new String(payload, StandardCharsets.UTF_8);
            throw new MqttSerializationException(
                    "Failed to deserialize MQTT payload into " + type.getSimpleName() + ": " + preview, ex);
        }
    }
}
