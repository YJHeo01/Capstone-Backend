package com.example.orderserver.dto;

import java.math.BigDecimal;

public record CampusPointResponse(
        BigDecimal latitude,
        BigDecimal longitude
) {
}
