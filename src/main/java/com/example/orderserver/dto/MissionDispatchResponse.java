package com.example.orderserver.dto;

public record MissionDispatchResponse(
        MissionResponse mission,
        boolean robotConnected,
        String dispatchMessage
) {
}
