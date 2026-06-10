package com.example.orderserver.dto;

import com.example.orderserver.domain.RobotRouteEventStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RobotRouteEventRequest(
        @NotBlank(message = "Command ID is required.")
        @Size(max = 100, message = "Command ID must be at most 100 characters.")
        String commandId,

        @NotNull(message = "Route event status is required.")
        RobotRouteEventStatus status,

        @NotNull(message = "Current waypoint sequence is required.")
        @Positive(message = "Current waypoint sequence must be positive.")
        Integer currentWaypointSequence,

        @NotNull(message = "Latitude is required.")
        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.")
        @DecimalMax(value = "90.0", message = "Latitude must be at most 90.")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required.")
        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.")
        @DecimalMax(value = "180.0", message = "Longitude must be at most 180.")
        BigDecimal longitude,

        @Size(max = 500, message = "Message must be at most 500 characters.")
        String message
) {
}
