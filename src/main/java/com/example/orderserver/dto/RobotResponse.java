package com.example.orderserver.dto;

import com.example.orderserver.domain.RobotStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RobotResponse(
        RobotStatus status,
        UUID currentMissionId,
        LocalDateTime updatedAt
) {
}
