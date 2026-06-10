package com.example.orderserver.domain;

import java.math.BigDecimal;

public record CampusPoint(
        BigDecimal latitude,
        BigDecimal longitude
) {
}
