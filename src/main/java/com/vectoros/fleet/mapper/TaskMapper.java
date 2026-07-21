package com.vectoros.fleet.mapper;

import com.vectoros.fleet.dto.TaskRequest;
import com.vectoros.fleet.dto.TaskResponse;
import com.vectoros.fleet.entity.Robot;
import com.vectoros.fleet.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        Robot robot = task.getAssignedRobot();
        return new TaskResponse(
                task.getId(),
                task.getTaskNumber(),
                task.getPickupLocation(),
                task.getDropoffLocation(),
                task.getPriority(),
                task.getStatus(),
                robot != null ? robot.getId() : null,
                robot != null ? robot.getName() : null,
                task.getEstimatedDistance(),
                task.getEstimatedDuration(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getCompletedAt()
        );
    }

    public Task toEntity(String taskNumber, TaskRequest request) {
        return Task.createNew(
                taskNumber,
                request.getPickupLocation(),
                request.getDropoffLocation(),
                request.getPriority()
        );
    }
}
