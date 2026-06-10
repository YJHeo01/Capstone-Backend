package com.example.orderserver.service;

import com.example.orderserver.domain.CampusEdge;
import com.example.orderserver.domain.CampusNode;
import com.example.orderserver.domain.CampusPoint;
import com.example.orderserver.domain.CampusRoute;
import com.example.orderserver.domain.CampusRouteLeg;
import com.example.orderserver.exception.CampusMapNodeNotFoundException;
import com.example.orderserver.exception.CampusRouteNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CampusMapService implements RouteService {

    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private final List<CampusNode> nodes;
    private final List<CampusEdge> edges;
    private final Map<String, CampusNode> nodesById;
    private final Map<String, List<AdjacentEdge>> graph;

    public CampusMapService() {
        this.nodes = List.of(
                node("library_front", "도서관 앞", "37.375226", "126.633868"),
                node("info_a", "정보대 A동", "37.374528", "126.633170"),
                node("info_b", "정보대 B동", "37.374806", "126.633435"),
                node("social_convention_intersection", "(사과대 / 컨벤션) 교차로", "37.375606", "126.633185"),
                node("social_science_front", "사과대 앞", "37.376190", "126.633746"),
                node("convention_front", "컨벤션 앞", "37.375258", "126.632858"),
                node("convention_near", "컨벤션 인근", "37.374917", "126.632551"),
                node("natural_science", "자연대", "37.375556", "126.634128")
        );
        this.nodesById = nodes.stream()
                .collect(Collectors.toUnmodifiableMap(CampusNode::id, Function.identity()));
        this.edges = List.of(
                edge("info_a_info_b", "info_a", "info_b"),
                edge("info_b_library_front", "info_b", "library_front"),
                edge("library_front_natural_science", "library_front", "natural_science"),
                edge("library_front_social_convention_intersection", "library_front", "social_convention_intersection"),
                edge("social_convention_intersection_convention_front", "social_convention_intersection", "convention_front"),
                edge("convention_front_convention_near", "convention_front", "convention_near"),
                edge("convention_near_info_a", "convention_near", "info_a"),
                edge("social_convention_intersection_social_science_front", "social_convention_intersection", "social_science_front")
        );
        this.graph = buildGraph(nodes, edges);
    }

    public List<CampusNode> getNodes() {
        return nodes;
    }

    public List<CampusEdge> getEdges() {
        return edges;
    }

    @Override
    public CampusRoute findRoute(String fromNodeId, String toNodeId) {
        CampusNode start = getNodeOrThrow(fromNodeId);
        CampusNode target = getNodeOrThrow(toNodeId);

        if (start.id().equals(target.id())) {
            return new CampusRoute(
                    start.id(),
                    target.id(),
                    List.of(start),
                    List.of(),
                    List.of(start.point()),
                    0.0
            );
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, PreviousStep> previousSteps = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::distance));

        distances.put(start.id(), 0.0);
        queue.add(new NodeDistance(start.id(), 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            if (current.distance() > distances.getOrDefault(current.nodeId(), Double.MAX_VALUE)) {
                continue;
            }
            if (current.nodeId().equals(target.id())) {
                break;
            }

            for (AdjacentEdge adjacentEdge : graph.getOrDefault(current.nodeId(), List.of())) {
                double nextDistance = roundToOne(current.distance() + adjacentEdge.edge().distanceM());
                double knownDistance = distances.getOrDefault(adjacentEdge.toNodeId(), Double.MAX_VALUE);
                if (nextDistance < knownDistance) {
                    distances.put(adjacentEdge.toNodeId(), nextDistance);
                    previousSteps.put(
                            adjacentEdge.toNodeId(),
                            new PreviousStep(current.nodeId(), adjacentEdge.edge(), adjacentEdge.forward())
                    );
                    queue.add(new NodeDistance(adjacentEdge.toNodeId(), nextDistance));
                }
            }
        }

        if (!distances.containsKey(target.id())) {
            throw new CampusRouteNotFoundException(start.id(), target.id());
        }

        List<CampusRouteLeg> legs = reconstructLegs(start.id(), target.id(), previousSteps);
        List<CampusNode> routeNodes = buildRouteNodes(start, legs);
        List<CampusPoint> waypoints = buildRouteWaypoints(start.point(), legs);

        return new CampusRoute(
                start.id(),
                target.id(),
                List.copyOf(routeNodes),
                List.copyOf(legs),
                List.copyOf(waypoints),
                roundToOne(distances.get(target.id()))
        );
    }

    @Override
    public boolean hasNode(String nodeId) {
        return nodesById.containsKey(nodeId);
    }

    private CampusNode getNodeOrThrow(String nodeId) {
        CampusNode node = nodesById.get(nodeId);
        if (node == null) {
            throw new CampusMapNodeNotFoundException(nodeId);
        }
        return node;
    }

    private List<CampusRouteLeg> reconstructLegs(
            String fromNodeId,
            String toNodeId,
            Map<String, PreviousStep> previousSteps
    ) {
        List<CampusRouteLeg> reversedLegs = new ArrayList<>();
        String cursor = toNodeId;

        while (!cursor.equals(fromNodeId)) {
            PreviousStep previousStep = previousSteps.get(cursor);
            if (previousStep == null) {
                throw new CampusRouteNotFoundException(fromNodeId, toNodeId);
            }
            reversedLegs.add(toRouteLeg(previousStep, cursor));
            cursor = previousStep.fromNodeId();
        }

        Collections.reverse(reversedLegs);
        return reversedLegs;
    }

    private CampusRouteLeg toRouteLeg(PreviousStep previousStep, String toNodeId) {
        CampusEdge edge = previousStep.edge();
        List<CampusPoint> waypoints = new ArrayList<>(edge.waypoints());
        if (!previousStep.forward()) {
            Collections.reverse(waypoints);
        }

        return new CampusRouteLeg(
                edge.id(),
                previousStep.fromNodeId(),
                toNodeId,
                edge.distanceM(),
                List.copyOf(waypoints)
        );
    }

    private List<CampusNode> buildRouteNodes(CampusNode start, List<CampusRouteLeg> legs) {
        List<CampusNode> routeNodes = new ArrayList<>();
        routeNodes.add(start);
        for (CampusRouteLeg leg : legs) {
            routeNodes.add(getNodeOrThrow(leg.toNodeId()));
        }
        return routeNodes;
    }

    private static List<CampusPoint> buildRouteWaypoints(CampusPoint startPoint, List<CampusRouteLeg> legs) {
        List<CampusPoint> routeWaypoints = new ArrayList<>();
        routeWaypoints.add(startPoint);

        for (CampusRouteLeg leg : legs) {
            for (CampusPoint point : leg.waypoints()) {
                if (!routeWaypoints.isEmpty() && routeWaypoints.get(routeWaypoints.size() - 1).equals(point)) {
                    continue;
                }
                routeWaypoints.add(point);
            }
        }

        return routeWaypoints;
    }

    private Map<String, List<AdjacentEdge>> buildGraph(List<CampusNode> nodes, List<CampusEdge> edges) {
        Map<String, List<AdjacentEdge>> mutableGraph = new LinkedHashMap<>();
        for (CampusNode node : nodes) {
            mutableGraph.put(node.id(), new ArrayList<>());
        }

        for (CampusEdge edge : edges) {
            mutableGraph.get(edge.fromNodeId()).add(new AdjacentEdge(edge, edge.toNodeId(), true));
            if (edge.bidirectional()) {
                mutableGraph.get(edge.toNodeId()).add(new AdjacentEdge(edge, edge.fromNodeId(), false));
            }
        }

        Map<String, List<AdjacentEdge>> immutableGraph = new LinkedHashMap<>();
        for (Map.Entry<String, List<AdjacentEdge>> entry : mutableGraph.entrySet()) {
            immutableGraph.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(immutableGraph);
    }

    private CampusEdge edge(String id, String fromNodeId, String toNodeId) {
        CampusPoint fromPoint = getNodeOrThrow(fromNodeId).point();
        CampusPoint toPoint = getNodeOrThrow(toNodeId).point();
        return new CampusEdge(
                id,
                fromNodeId,
                toNodeId,
                distanceMeters(fromPoint, toPoint),
                true,
                List.of(fromPoint, toPoint)
        );
    }

    private static CampusNode node(String id, String name, String latitude, String longitude) {
        return new CampusNode(
                id,
                name,
                new CampusPoint(new BigDecimal(latitude), new BigDecimal(longitude))
        );
    }

    private static double distanceMeters(CampusPoint from, CampusPoint to) {
        double lat1 = Math.toRadians(from.latitude().doubleValue());
        double lat2 = Math.toRadians(to.latitude().doubleValue());
        double deltaLat = Math.toRadians(to.latitude().doubleValue() - from.latitude().doubleValue());
        double deltaLng = Math.toRadians(to.longitude().doubleValue() - from.longitude().doubleValue());

        double haversine = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double distance = 2 * EARTH_RADIUS_M * Math.asin(Math.min(1.0, Math.sqrt(haversine)));

        return roundToOne(distance);
    }

    private static double roundToOne(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record AdjacentEdge(CampusEdge edge, String toNodeId, boolean forward) {
    }

    private record PreviousStep(String fromNodeId, CampusEdge edge, boolean forward) {
    }

    private record NodeDistance(String nodeId, double distance) {
    }
}
