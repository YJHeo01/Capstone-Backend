package com.example.orderserver.dto;

import com.example.orderserver.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required.")
        OrderStatus status
) {
}
