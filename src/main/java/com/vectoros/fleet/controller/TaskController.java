package com.vectoros.fleet.controller;

import com.vectoros.fleet.dto.TaskAssignmentRequest;
import com.vectoros.fleet.dto.TaskRequest;
import com.vectoros.fleet.dto.TaskResponse;
import com.vectoros.fleet.dto.TaskUpdateRequest;
import com.vectoros.fleet.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Warehouse task management and robot assignment")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create task", description = "Creates a new warehouse task.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "409", description = "Pickup and dropoff are identical"),
            @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Returns every warehouse task.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task", description = "Returns a single task by id.")
    @Parameter(name = "id", description = "Task id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<TaskResponse> getTask(@PathVariable("id") Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates task information (status).")
    @Parameter(name = "id", description = "Task id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Updates the status of a task.")
    @Parameter(name = "id", description = "Task id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition")
    })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, request));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign robot", description = "Assigns a robot to a task.")
    @Parameter(name = "id", description = "Task id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Task or robot not found"),
            @ApiResponse(responseCode = "409", description = "Robot unavailable or task not assignable")
    })
    public ResponseEntity<TaskResponse> assignRobot(
            @PathVariable("id") Long id,
            @Valid @RequestBody TaskAssignmentRequest request) {
        return ResponseEntity.ok(taskService.assignRobot(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task",
            description = "Deletes a task. Only COMPLETED, FAILED, or CANCELLED tasks may be deleted.")
    @Parameter(name = "id", description = "Task id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Task is not in a terminal status")
    })
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
