package com.example.orderserver.controller;

import com.example.orderserver.domain.CampusEdge;
import com.example.orderserver.domain.CampusNode;
import com.example.orderserver.domain.CampusPoint;
import com.example.orderserver.domain.CampusRoute;
import com.example.orderserver.domain.CampusRouteLeg;
import com.example.orderserver.dto.CampusEdgeResponse;
import com.example.orderserver.dto.CampusMapResponse;
import com.example.orderserver.dto.CampusNodeResponse;
import com.example.orderserver.dto.CampusPointResponse;
import com.example.orderserver.dto.CampusRouteLegResponse;
import com.example.orderserver.dto.CampusRouteResponse;

import java.util.List;

public final class CampusMapMapper {

    private CampusMapMapper() {
    }

    public static CampusMapResponse toMapResponse(List<CampusNode> nodes, List<CampusEdge> edges) {
        return new CampusMapResponse(
                nodes.stream().map(CampusMapMapper::toResponse).toList(),
                edges.stream().map(CampusMapMapper::toResponse).toList()
        );
    }

    public static CampusRouteResponse toResponse(CampusRoute route) {
        return new CampusRouteResponse(
                route.fromNodeId(),
                route.toNodeId(),
                route.nodes().stream().map(CampusNode::id).toList(),
                route.nodes().stream().map(CampusMapMapper::toResponse).toList(),
                route.legs().stream().map(CampusMapMapper::toResponse).toList(),
                route.waypoints().stream().map(CampusMapMapper::toResponse).toList(),
                route.totalDistanceM()
        );
    }

    private static CampusNodeResponse toResponse(CampusNode node) {
        return new CampusNodeResponse(
                node.id(),
                node.name(),
                node.point().latitude(),
                node.point().longitude()
        );
    }

    private static CampusEdgeResponse toResponse(CampusEdge edge) {
        return new CampusEdgeResponse(
                edge.id(),
                edge.fromNodeId(),
                edge.toNodeId(),
                edge.distanceM(),
                edge.bidirectional(),
                edge.waypoints().stream().map(CampusMapMapper::toResponse).toList()
        );
    }

    private static CampusRouteLegResponse toResponse(CampusRouteLeg leg) {
        return new CampusRouteLegResponse(
                leg.edgeId(),
                leg.fromNodeId(),
                leg.toNodeId(),
                leg.distanceM(),
                leg.waypoints().stream().map(CampusMapMapper::toResponse).toList()
        );
    }

    private static CampusPointResponse toResponse(CampusPoint point) {
        return new CampusPointResponse(
                point.latitude(),
                point.longitude()
        );
    }
}
