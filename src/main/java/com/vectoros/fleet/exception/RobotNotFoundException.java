package com.vectoros.fleet.exception;

public class RobotNotFoundException extends RuntimeException {

    public RobotNotFoundException(Long robotId) {
        super("Robot not found: id=" + robotId);
    }
}

