package com.example.orderserver.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RobotLocationResponse(
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime updatedAt
) {
}
