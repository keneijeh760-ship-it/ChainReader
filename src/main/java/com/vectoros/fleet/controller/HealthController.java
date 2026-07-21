package com.vectoros.fleet.controller;

import com.vectoros.fleet.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Service health monitoring")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns the current service health status.")
    public HealthResponse health() {
        return new HealthResponse("UP");
    }
}
