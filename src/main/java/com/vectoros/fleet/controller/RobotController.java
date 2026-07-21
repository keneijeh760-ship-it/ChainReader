package com.vectoros.fleet.controller;

import com.vectoros.fleet.dto.RobotRequest;
import com.vectoros.fleet.dto.RobotResponse;
import com.vectoros.fleet.dto.RobotUpdateRequest;
import com.vectoros.fleet.service.RobotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/robots")
@Tag(name = "Robots", description = "Robot registration and management")
public class RobotController {

    private final RobotService robotService;

    public RobotController(RobotService robotService) {
        this.robotService = robotService;
    }

    @PostMapping
    @Operation(summary = "Register robot", description = "Registers a new robot with the fleet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "409", description = "Conflict: duplicate robot name")
    })
    public ResponseEntity<RobotResponse> registerRobot(
            @Valid @RequestBody RobotRequest request
    ) {
        RobotResponse response = robotService.registerRobot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all robots", description = "Returns every robot.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<List<RobotResponse>> getAllRobots() {
        return ResponseEntity.ok(robotService.getAllRobots());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get robot", description = "Returns a single robot by id.")
    @Parameter(name = "id", description = "Robot id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<RobotResponse> getRobot(@PathVariable("id") Long id) {
        return ResponseEntity.ok(robotService.getRobot(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update robot", description = "Updates robot information (status).")
    @Parameter(name = "id", description = "Robot id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<RobotResponse> updateRobot(
            @PathVariable("id") Long id,
            @Valid @RequestBody RobotUpdateRequest request
    ) {
        return ResponseEntity.ok(robotService.updateRobot(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete robot", description = "Deletes a robot by id.")
    @Parameter(name = "id", description = "Robot id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteRobot(@PathVariable("id") Long id) {
        robotService.deleteRobot(id);
        return ResponseEntity.noContent().build();
    }
}

