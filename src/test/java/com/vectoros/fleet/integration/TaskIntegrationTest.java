package com.vectoros.fleet.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectoros.fleet.dto.RobotRequest;
import com.vectoros.fleet.dto.TaskAssignmentRequest;
import com.vectoros.fleet.dto.TaskRequest;
import com.vectoros.fleet.dto.TaskUpdateRequest;
import com.vectoros.fleet.entity.Priority;
import com.vectoros.fleet.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class TaskIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void taskCrudAndAssignmentFlow() throws Exception {
        MvcResult robotResult = mockMvc.perform(post("/api/v1/robots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RobotRequest("Robot-01"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long robotId = objectMapper.readTree(robotResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("A12", "C18", Priority.HIGH))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskNumber", is("TASK-000001")))
                .andExpect(jsonPath("$.status", is("NEW")))
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/v1/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pickupLocation", is("A12")));

        mockMvc.perform(patch("/api/v1/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskUpdateRequest(TaskStatus.PENDING))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));

        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskAssignmentRequest(robotId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ASSIGNED")))
                .andExpect(jsonPath("$.assignedRobotId", is(robotId.intValue())));

        mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskUpdateRequest(TaskStatus.COMPLETED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        mockMvc.perform(delete("/api/v1/tasks/" + taskId))
                .andExpect(status().isNoContent());
    }

    @Test
    void createTask_returnsValidationError_whenPickupIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("", "C18", Priority.HIGH))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void getTask_returnsNotFound_whenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void assignRobot_returnsNotFound_whenRobotDoesNotExist() throws Exception {
        MvcResult taskResult = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("A12", "C18", Priority.MEDIUM))))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/v1/tasks/" + taskId + "/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskAssignmentRequest(99999L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
