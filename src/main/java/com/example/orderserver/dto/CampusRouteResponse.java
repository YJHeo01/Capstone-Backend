package com.example.orderserver.dto;

import java.util.List;

public record CampusRouteResponse(
        String fromNodeId,
        String toNodeId,
        List<String> nodeIds,
        List<CampusNodeResponse> nodes,
        List<CampusRouteLegResponse> legs,
        List<CampusPointResponse> waypoints,
        double totalDistanceM
) {
}
