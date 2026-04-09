package com.example.orderserver.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
