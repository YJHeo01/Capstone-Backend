package com.example.orderserver.dto;

import java.util.List;

public record CampusEdgeResponse(
        String id,
        String fromNodeId,
        String toNodeId,
        double distanceM,
        boolean bidirectional,
        List<CampusPointResponse> waypoints
) {
}
