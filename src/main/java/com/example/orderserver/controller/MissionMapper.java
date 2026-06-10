package com.example.orderserver.controller;

import com.example.orderserver.domain.Mission;
import com.example.orderserver.domain.MissionWaypoint;
import com.example.orderserver.domain.Robot;
import com.example.orderserver.dto.MissionResponse;
import com.example.orderserver.dto.MissionWaypointResponse;
import com.example.orderserver.dto.RobotMissionCommandMessage;
import com.example.orderserver.dto.RobotResponse;

public final class MissionMapper {

    private MissionMapper() {
    }

    public static MissionResponse toResponse(Mission mission) {
        return new MissionResponse(
                mission.getMissionId(),
                mission.getOrderId(),
                mission.getType(),
                mission.getStatus(),
                mission.getFromNodeId(),
                mission.getToNodeId(),
                mission.getWaypoints().stream().map(MissionMapper::toResponse).toList(),
                mission.getLastMessage(),
                mission.getCreatedAt(),
                mission.getUpdatedAt(),
                mission.getDispatchedAt(),
                mission.getAckedAt(),
                mission.getStartedAt(),
                mission.getArrivedAt(),
                mission.getCompletedAt()
        );
    }

    public static RobotMissionCommandMessage toCommandMessage(Mission mission) {
        return new RobotMissionCommandMessage(
                "MISSION_ROUTE",
                mission.getMissionId(),
                mission.getOrderId(),
                mission.getType(),
                mission.getFromNodeId(),
                mission.getToNodeId(),
                mission.getWaypoints().stream().map(MissionMapper::toResponse).toList()
        );
    }

    public static RobotResponse toResponse(Robot robot) {
        return new RobotResponse(
                robot.getStatus(),
                robot.getCurrentMissionId(),
                robot.getUpdatedAt()
        );
    }

    private static MissionWaypointResponse toResponse(MissionWaypoint waypoint) {
        return new MissionWaypointResponse(
                waypoint.getSequence(),
                waypoint.getLatitude(),
                waypoint.getLongitude()
        );
    }
}
