package com.vectoros.fleet.exception;

public class RobotUnavailableException extends RuntimeException {

    public RobotUnavailableException(Long robotId) {
        super("Robot is unavailable for task assignment: id=" + robotId);
    }
}
