package com.example.orderserver.service;

import com.example.orderserver.domain.Mission;

public record MissionDispatchResult(
        Mission mission,
        boolean robotConnected,
        String dispatchMessage
) {
}
