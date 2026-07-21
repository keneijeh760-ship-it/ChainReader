package com.vectoros.fleet.dto;

import com.vectoros.fleet.entity.Priority;
import com.vectoros.fleet.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.time.Instant;

@Value
@Schema(description = "Warehouse task information returned to API consumers.")
public class TaskResponse {

    @Schema(description = "Task identifier.", example = "1")
    Long id;

    @Schema(description = "Human-readable task number.", example = "TASK-000001")
    String taskNumber;

    @Schema(description = "Pickup location.", example = "A12")
    String pickupLocation;

    @Schema(description = "Dropoff location.", example = "C18")
    String dropoffLocation;

    @Schema(description = "Task priority.", example = "HIGH")
    Priority priority;

    @Schema(description = "Current task status.", example = "NEW")
    TaskStatus status;

    @Schema(description = "ID of the assigned robot, or null if unassigned.", example = "3")
    Long assignedRobotId;

    @Schema(description = "Name of the assigned robot, or null if unassigned.", example = "Robot-03")
    String assignedRobotName;

    @Schema(description = "Estimated distance in metres.", example = "42.5")
    Double estimatedDistance;

    @Schema(description = "Estimated duration in seconds.", example = "120")
    Integer estimatedDuration;

    @Schema(description = "Timestamp when the task was created.")
    Instant createdAt;

    @Schema(description = "Timestamp when the task was last updated.")
    Instant updatedAt;

    @Schema(description = "Timestamp when the task was completed or failed.")
    Instant completedAt;
}
