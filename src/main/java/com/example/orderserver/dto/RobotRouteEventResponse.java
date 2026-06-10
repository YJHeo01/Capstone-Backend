package com.example.orderserver.dto;

import com.example.orderserver.domain.RobotRouteEventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RobotRouteEventResponse(
        Long eventId,
        String commandId,
        RobotRouteEventStatus status,
        Integer currentWaypointSequence,
        BigDecimal latitude,
        BigDecimal longitude,
        String message,
        LocalDateTime receivedAt
) {
}
