package com.vectoros.fleet.dto;

import com.vectoros.fleet.entity.RobotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
@Schema(description = "Robot information returned to API consumers.")
public class RobotResponse {

    @Schema(description = "Robot identifier.", example = "1")
    Long id;

    @Schema(description = "Unique robot name.", example = "Robot-01")
    String name;

    @Schema(description = "Current robot status.", example = "IDLE")
    RobotStatus status;

    @Schema(description = "Current battery level (percentage).", example = "100")
    Integer batteryLevel;

    @Schema(description = "Current X position.", example = "0")
    Double currentX;

    @Schema(description = "Current Y position.", example = "0")
    Double currentY;
}

