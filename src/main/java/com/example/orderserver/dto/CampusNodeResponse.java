package com.example.orderserver.dto;

import java.math.BigDecimal;

public record CampusNodeResponse(
        String id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
