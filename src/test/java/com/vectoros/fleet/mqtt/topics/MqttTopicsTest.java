package com.vectoros.fleet.mqtt.topics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MqttTopicsTest {

    @Test
    void topicConstantsMatchContract() {
        assertEquals("warehouse/tasks/assigned", MqttTopics.TASKS_ASSIGNED);
        assertEquals("warehouse/tasks/cancelled", MqttTopics.TASKS_CANCELLED);
        assertEquals("warehouse/tasks/status", MqttTopics.TASKS_STATUS);
        assertEquals("warehouse/tasks/completed", MqttTopics.TASKS_COMPLETED);
        assertEquals("warehouse/robots/status", MqttTopics.ROBOTS_STATUS);
        assertEquals("warehouse/robots/telemetry", MqttTopics.ROBOTS_TELEMETRY);
        assertEquals("warehouse/system/heartbeat", MqttTopics.SYSTEM_HEARTBEAT);
    }
}
