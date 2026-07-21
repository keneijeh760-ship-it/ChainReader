package com.vectoros.fleet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectoros.fleet.dto.TaskRequest;
import com.vectoros.fleet.dto.TaskResponse;
import com.vectoros.fleet.dto.TaskUpdateRequest;
import com.vectoros.fleet.entity.Priority;
import com.vectoros.fleet.entity.TaskStatus;
import com.vectoros.fleet.exception.GlobalExceptionHandler;
import com.vectoros.fleet.exception.TaskNotFoundException;
import com.vectoros.fleet.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TaskService taskService;

    @Test
    void createTask_returnsCreated_whenRequestIsValid() throws Exception {
        TaskResponse response = new TaskResponse(
                1L, "TASK-000001", "A12", "C18", Priority.HIGH,
                TaskStatus.NEW, null, null, null, null,
                Instant.now(), Instant.now(), null
        );
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("A12", "C18", Priority.HIGH))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskNumber", is("TASK-000001")))
                .andExpect(jsonPath("$.status", is("NEW")));
    }

    @Test
    void createTask_returnsValidationError_whenPickupIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("", "C18", Priority.HIGH))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors", not(empty())));
    }

    @Test
    void createTask_returnsValidationError_whenPriorityIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pickupLocation\":\"A12\",\"dropoffLocation\":\"C18\",\"priority\":null}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void updateTask_returnsUpdatedTask_whenRequestIsValid() throws Exception {
        TaskResponse response = new TaskResponse(
                1L, "TASK-000001", "A12", "C18", Priority.HIGH,
                TaskStatus.PENDING, null, null, null, null,
                Instant.now(), Instant.now(), null
        );
        when(taskService.updateTask(eq(1L), any(TaskUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskUpdateRequest(TaskStatus.PENDING))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void getTask_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        when(taskService.getTask(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Task not found")));
    }
}
