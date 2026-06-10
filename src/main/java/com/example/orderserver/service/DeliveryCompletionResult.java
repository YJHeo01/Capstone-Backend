package com.example.orderserver.service;

import com.example.orderserver.domain.Mission;

public record DeliveryCompletionResult(
        Mission deliveryMission,
        Mission returnMission,
        boolean robotConnected,
        boolean returnMissionCreated,
        String dispatchMessage
) {
}
