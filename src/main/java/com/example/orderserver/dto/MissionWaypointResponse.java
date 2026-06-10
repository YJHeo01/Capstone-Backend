package com.example.orderserver.dto;

import java.math.BigDecimal;

public record MissionWaypointResponse(
        Integer sequence,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
