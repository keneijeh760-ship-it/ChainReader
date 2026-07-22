package com.vectoros.fleet.mqtt.topics;

/**
 * Central MQTT topic registry for the VectorOS platform.
 * <p>
 * All topic names must be defined here. Hardcoded topic strings are prohibited.
 */
public final class MqttTopics {

    public static final String TASKS_ASSIGNED = "warehouse/tasks/assigned";
    public static final String TASKS_CANCELLED = "warehouse/tasks/cancelled";
    public static final String TASKS_STATUS = "warehouse/tasks/status";
    public static final String TASKS_COMPLETED = "warehouse/tasks/completed";
    public static final String ROBOTS_STATUS = "warehouse/robots/status";
    public static final String ROBOTS_TELEMETRY = "warehouse/robots/telemetry";
    public static final String SYSTEM_HEARTBEAT = "warehouse/system/heartbeat";

    private MqttTopics() {
    }
}
