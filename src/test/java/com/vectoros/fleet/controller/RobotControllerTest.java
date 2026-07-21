package com.vectoros.fleet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectoros.fleet.dto.RobotRequest;
import com.vectoros.fleet.dto.RobotResponse;
import com.vectoros.fleet.entity.RobotStatus;
import com.vectoros.fleet.exception.GlobalExceptionHandler;
import com.vectoros.fleet.exception.RobotNotFoundException;
import com.vectoros.fleet.service.RobotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RobotController.class)
@Import(GlobalExceptionHandler.class)
class RobotControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RobotService robotService;

    @Test
    void registerRobot_returnsValidationError_whenNameBlank() throws Exception {
        RobotRequest request = new RobotRequest("");

        mockMvc.perform(post("/api/v1/robots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed.")))
                .andExpect(jsonPath("$.errors", not(empty())))
                .andExpect(jsonPath("$.errors[0]", containsString("name")));
    }

    @Test
    void registerRobot_returnsCreatedRobot_whenRequestValid() throws Exception {
        RobotResponse response = new RobotResponse(
                1L,
                "Robot-01",
                RobotStatus.IDLE,
                100,
                0d,
                0d
        );

        when(robotService.registerRobot(any(RobotRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/robots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RobotRequest("Robot-01"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Robot-01")))
                .andExpect(jsonPath("$.status", is("IDLE")));
    }

    @Test
    void getRobot_returnsNotFoundError_whenRobotMissing() throws Exception {
        when(robotService.getRobot(1L)).thenThrow(new RobotNotFoundException(1L));

        mockMvc.perform(get("/api/v1/robots/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Robot not found")));
    }

    @Test
    void deleteRobot_returnsNoContent_whenRobotExists() throws Exception {
        doNothing().when(robotService).deleteRobot(1L);

        mockMvc.perform(delete("/api/v1/robots/1"))
                .andExpect(status().isNoContent());
    }
}

