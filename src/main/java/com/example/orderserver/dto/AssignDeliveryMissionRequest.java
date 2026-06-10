package com.example.orderserver.dto;

import jakarta.validation.constraints.Size;

public record AssignDeliveryMissionRequest(
        @Size(max = 100, message = "From node ID must be at most 100 characters.")
        String fromNodeId,

        @Size(max = 100, message = "Destination node ID must be at most 100 characters.")
        String destinationNodeId
) {
}
