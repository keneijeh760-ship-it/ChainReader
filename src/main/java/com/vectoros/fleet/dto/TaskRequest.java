package com.vectoros.fleet.dto;

import com.vectoros.fleet.entity.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@Schema(description = "Request payload to create a new warehouse task.")
public class TaskRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Location to pick up the goods.", example = "A12")
    String pickupLocation;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Location to drop off the goods.", example = "C18")
    String dropoffLocation;

    @NotNull
    @Schema(description = "Task priority.", example = "HIGH")
    Priority priority;
}
