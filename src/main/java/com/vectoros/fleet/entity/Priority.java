package com.vectoros.fleet.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Priority level of a warehouse task.")
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
