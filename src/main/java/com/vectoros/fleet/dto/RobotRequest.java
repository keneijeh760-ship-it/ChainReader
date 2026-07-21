package com.vectoros.fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@Schema(description = "Request payload to register a new robot.")
public class RobotRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Unique robot name.", example = "Robot-01")
    String name;
}

