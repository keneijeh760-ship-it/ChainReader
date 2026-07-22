package com.vectoros.fleet.mqtt.events;

import com.vectoros.fleet.entity.Priority;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class TaskAssignedEvent {

    Long taskId;
    String taskNumber;
    Long robotId;
    String pickupLocation;
    String dropoffLocation;
    Priority priority;
    Instant timestamp;
}
