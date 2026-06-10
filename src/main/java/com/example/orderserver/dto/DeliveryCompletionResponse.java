package com.example.orderserver.dto;

public record DeliveryCompletionResponse(
        MissionResponse deliveryMission,
        MissionResponse returnMission,
        boolean robotConnected,
        boolean returnMissionCreated,
        String dispatchMessage
) {
}
