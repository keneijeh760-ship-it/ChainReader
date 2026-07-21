package com.vectoros.fleet.exception;

public class DuplicateRobotException extends RuntimeException {

    public DuplicateRobotException(String robotName) {
        super("Duplicate robot name: " + robotName);
    }
}

