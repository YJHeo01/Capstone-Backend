package com.example.orderserver.exception;

public class CampusRouteNotFoundException extends RuntimeException {

    public CampusRouteNotFoundException(String fromNodeId, String toNodeId) {
        super("Campus route not found. from=" + fromNodeId + ", to=" + toNodeId);
    }
}
