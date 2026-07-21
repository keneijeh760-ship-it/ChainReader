package com.vectoros.fleet.dto;

import com.vectoros.fleet.entity.RobotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
@Schema(description = "Request payload to update an existing robot.")
public class RobotUpdateRequest {

    @NotNull
    @Schema(description = "New operational status.", example = "WORKING")
    RobotStatus status;
}

