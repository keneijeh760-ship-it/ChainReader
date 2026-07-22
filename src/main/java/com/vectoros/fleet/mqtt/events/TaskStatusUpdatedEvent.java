package com.vectoros.fleet.mqtt.events;

import com.vectoros.fleet.entity.TaskStatus;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class TaskStatusUpdatedEvent {

    Long taskId;
    TaskStatus status;
    Instant timestamp;
}
