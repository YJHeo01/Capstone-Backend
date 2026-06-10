package com.example.orderserver.dto;

import jakarta.validation.constraints.Size;

public record CompleteDeliveryRequest(
        @Size(max = 500, message = "Message must be at most 500 characters.")
        String message
) {
}
