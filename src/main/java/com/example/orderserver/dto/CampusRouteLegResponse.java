package com.example.orderserver.dto;

import java.util.List;

public record CampusRouteLegResponse(
        String edgeId,
        String fromNodeId,
        String toNodeId,
        double distanceM,
        List<CampusPointResponse> waypoints
) {
}
