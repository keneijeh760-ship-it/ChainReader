package com.vectoros.fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Request payload to assign a robot to a task.")
public class TaskAssignmentRequest {

    @NotNull
    @Schema(description = "ID of the robot to assign.", example = "3")
    Long robotId;
}
