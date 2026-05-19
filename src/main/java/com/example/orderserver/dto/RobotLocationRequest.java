package com.example.orderserver.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RobotLocationRequest(
        @NotNull(message = "Latitude is required.")
        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.")
        @DecimalMax(value = "90.0", message = "Latitude must be at most 90.")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required.")
        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.")
        @DecimalMax(value = "180.0", message = "Longitude must be at most 180.")
        BigDecimal longitude
) {
}
