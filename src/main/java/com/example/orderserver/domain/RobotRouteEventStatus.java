package com.example.orderserver.domain;

public enum RobotRouteEventStatus {
    ACCEPTED,
    MOVING,
    WAYPOINT_REACHED,
    ARRIVED,
    FAILED,
    CANCELED
}
