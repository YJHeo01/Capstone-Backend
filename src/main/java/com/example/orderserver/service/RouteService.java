package com.example.orderserver.service;

import com.example.orderserver.domain.CampusRoute;

public interface RouteService {

    CampusRoute findRoute(String fromNodeId, String toNodeId);

    boolean hasNode(String nodeId);
}
