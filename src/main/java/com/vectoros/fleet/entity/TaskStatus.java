package com.vectoros.fleet.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a warehouse task.")
public enum TaskStatus {
    NEW,
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}
