package com.vectoros.fleet.service;

import com.vectoros.fleet.dto.TaskAssignmentRequest;
import com.vectoros.fleet.dto.TaskRequest;
import com.vectoros.fleet.dto.TaskResponse;
import com.vectoros.fleet.dto.TaskUpdateRequest;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskRequest request);

    TaskResponse getTask(Long taskId);

    List<TaskResponse> getAllTasks();

    TaskResponse updateTask(Long taskId, TaskUpdateRequest request);

    TaskResponse updateTaskStatus(Long taskId, TaskUpdateRequest request);

    TaskResponse assignRobot(Long taskId, TaskAssignmentRequest request);

    void deleteTask(Long taskId);
}
