package com.vectoros.fleet.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operational status of a robot.")
public enum RobotStatus {
    IDLE,
    WORKING,
    CHARGING,
    OFFLINE,
    ERROR
}

