package com.example.orderserver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "Customer name is required.")
        String customerName,

        @NotBlank(message = "Phone number is required.")
        String phoneNumber,

        @NotBlank(message = "Delivery address is required.")
        String deliveryAddress,

        @NotEmpty(message = "At least one item is required.")
        List<@Valid CreateOrderItemRequest> items
) {
}
