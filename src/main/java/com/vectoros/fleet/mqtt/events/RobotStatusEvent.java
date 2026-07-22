package com.vectoros.fleet.mqtt.events;

import com.vectoros.fleet.entity.RobotStatus;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class RobotStatusEvent {

    Long robotId;
    RobotStatus status;
    Integer batteryLevel;
    Instant timestamp;
}
