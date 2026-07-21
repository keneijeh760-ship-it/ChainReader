package com.vectoros.fleet.dto;

import com.vectoros.fleet.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Request payload to update the status of an existing task.")
public class TaskUpdateRequest {

    @NotNull
    @Schema(description = "New task status.", example = "IN_PROGRESS")
    TaskStatus status;
}
