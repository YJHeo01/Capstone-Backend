package com.example.orderserver.dto;

import com.example.orderserver.domain.OrderSource;
import com.example.orderserver.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderSource source,
        String customerName,
        String phoneNumber,
        String deliveryAddress,
        List<OrderItemResponse> items,
        BigDecimal totalPrice,
        OrderStatus status,
        boolean sentToRobot,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
