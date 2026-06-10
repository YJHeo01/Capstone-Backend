package com.example.orderserver.domain;

import java.util.List;

public record CampusRouteLeg(
        String edgeId,
        String fromNodeId,
        String toNodeId,
        double distanceM,
        List<CampusPoint> waypoints
) {
}
