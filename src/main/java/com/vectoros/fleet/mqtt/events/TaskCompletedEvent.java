package com.vectoros.fleet.mqtt.events;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class TaskCompletedEvent {

    Long taskId;
    Long robotId;
    Instant completedAt;
}
