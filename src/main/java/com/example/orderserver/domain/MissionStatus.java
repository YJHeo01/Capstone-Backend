package com.example.orderserver.domain;

public enum MissionStatus {
    CREATED,
    DISPATCHED,
    ACKED,
    IN_PROGRESS,
    ARRIVED,
    COMPLETED,
    FAILED,
    CANCELED
}
