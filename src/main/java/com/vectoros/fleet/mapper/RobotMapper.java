package com.vectoros.fleet.mapper;

import com.vectoros.fleet.dto.RobotRequest;
import com.vectoros.fleet.dto.RobotResponse;
import com.vectoros.fleet.dto.RobotUpdateRequest;
import com.vectoros.fleet.entity.Robot;
import org.springframework.stereotype.Component;

@Component
public class RobotMapper {

    public RobotResponse toResponse(Robot robot) {
        return new RobotResponse(
                robot.getId(),
                robot.getName(),
                robot.getStatus(),
                robot.getBatteryLevel(),
                robot.getCurrentX(),
                robot.getCurrentY()
        );
    }

    public Robot toEntity(RobotRequest request) {
        return Robot.createNew(request.getName());
    }

    public void applyUpdate(RobotUpdateRequest request, Robot robot) {
        robot.updateStatus(request.getStatus());
    }
}

