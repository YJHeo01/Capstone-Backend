package com.example.orderserver.domain;

public record CampusNode(
        String id,
        String name,
        CampusPoint point
) {
}
