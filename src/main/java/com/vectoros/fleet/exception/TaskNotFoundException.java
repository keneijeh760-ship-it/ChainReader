package com.vectoros.fleet.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long taskId) {
        super("Task not found: id=" + taskId);
    }

    public TaskNotFoundException(String taskNumber) {
        super("Task not found: taskNumber=" + taskNumber);
    }
}
