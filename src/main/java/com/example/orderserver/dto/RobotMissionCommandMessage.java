package com.example.orderserver.dto;

import com.example.orderserver.domain.MissionType;

import java.util.List;
import java.util.UUID;

public record RobotMissionCommandMessage(
        String messageType,
        UUID missionId,
        UUID orderId,
        MissionType missionType,
        String fromNodeId,
        String toNodeId,
        List<MissionWaypointResponse> waypoints
) {
}
