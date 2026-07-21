package com.vectoros.fleet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.List;

@Value
public class ApiErrorResponse {

    @Schema(description = "Always false for error responses.", example = "false")
    boolean success;

    @Schema(description = "Human-readable error message.", example = "Robot not found.")
    String message;

    @Schema(description = "List of specific validation or processing errors.", example = "[\"name must not be blank\"]")
    List<String> errors;

    public static ApiErrorResponse of(String message, List<String> errors) {
        return new ApiErrorResponse(false, message, errors);
    }
}

