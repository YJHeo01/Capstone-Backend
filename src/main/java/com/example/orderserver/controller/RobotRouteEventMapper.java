package com.example.orderserver.controller;

import com.example.orderserver.domain.RobotRouteEvent;
import com.example.orderserver.dto.RobotRouteEventResponse;

public final class RobotRouteEventMapper {

    private RobotRouteEventMapper() {
    }

    public static RobotRouteEventResponse toResponse(RobotRouteEvent event) {
        return new RobotRouteEventResponse(
                event.getEventId(),
                event.getCommandId(),
                event.getStatus(),
                event.getCurrentWaypointSequence(),
                event.getLatitude(),
                event.getLongitude(),
                event.getMessage(),
                event.getReceivedAt()
        );
    }
}
