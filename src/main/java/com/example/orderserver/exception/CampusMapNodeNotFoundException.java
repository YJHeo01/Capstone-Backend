package com.example.orderserver.exception;

public class CampusMapNodeNotFoundException extends RuntimeException {

    public CampusMapNodeNotFoundException(String nodeId) {
        super("Campus map node not found. nodeId=" + nodeId);
    }
}
