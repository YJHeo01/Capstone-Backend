package com.example.orderserver.dto;

import com.example.orderserver.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusResponse(
        UUID orderId,
        OrderStatus status,
        boolean sentToRobot,
        LocalDateTime updatedAt,
        RobotLocationResponse robotLocation
) {
}
