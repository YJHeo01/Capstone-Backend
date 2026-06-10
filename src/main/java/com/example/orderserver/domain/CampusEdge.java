package com.example.orderserver.domain;

import java.util.List;

public record CampusEdge(
        String id,
        String fromNodeId,
        String toNodeId,
        double distanceM,
        boolean bidirectional,
        List<CampusPoint> waypoints
) {
}
