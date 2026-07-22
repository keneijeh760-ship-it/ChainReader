package com.vectoros.fleet.mqtt.events;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class RobotTelemetryEvent {

    Long robotId;
    Integer batteryLevel;
    Double currentX;
    Double currentY;
    Double speed;
    Double heading;
    Instant timestamp;
}
