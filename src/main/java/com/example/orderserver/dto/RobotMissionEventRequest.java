package com.example.orderserver.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record RobotMissionEventRequest(
        @NotNull(message = "Mission ID is required.")
        UUID missionId,

        @NotNull(message = "Mission event type is required.")
        RobotMissionEventType eventType,

        @Positive(message = "Current waypoint sequence must be positive.")
        Integer currentWaypointSequence,

        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.")
        @DecimalMax(value = "90.0", message = "Latitude must be at most 90.")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.")
        @DecimalMax(value = "180.0", message = "Longitude must be at most 180.")
        BigDecimal longitude,

        @Size(max = 500, message = "Message must be at most 500 characters.")
        String message
) {
}
