package com.example.orderserver.dto;

import com.example.orderserver.domain.MissionStatus;
import com.example.orderserver.domain.MissionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MissionResponse(
        UUID missionId,
        UUID orderId,
        MissionType type,
        MissionStatus status,
        String fromNodeId,
        String toNodeId,
        List<MissionWaypointResponse> waypoints,
        String lastMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime dispatchedAt,
        LocalDateTime ackedAt,
        LocalDateTime startedAt,
        LocalDateTime arrivedAt,
        LocalDateTime completedAt
) {
}
