package com.example.orderserver.domain;

import java.util.List;

public record CampusRoute(
        String fromNodeId,
        String toNodeId,
        List<CampusNode> nodes,
        List<CampusRouteLeg> legs,
        List<CampusPoint> waypoints,
        double totalDistanceM
) {
}
