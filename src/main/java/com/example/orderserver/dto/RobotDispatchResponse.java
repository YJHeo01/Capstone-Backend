package com.example.orderserver.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RobotDispatchResponse(
        UUID dispatchId,
        UUID orderId,
        String targetRobot,
        String message,
        LocalDateTime dispatchedAt
) {
}
