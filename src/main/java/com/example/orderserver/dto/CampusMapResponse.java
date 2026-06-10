package com.example.orderserver.dto;

import java.util.List;

public record CampusMapResponse(
        List<CampusNodeResponse> nodes,
        List<CampusEdgeResponse> edges
) {
}
