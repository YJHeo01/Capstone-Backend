package com.example.orderserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class MissionWaypoint {

    @Column(name = "waypoint_sequence", nullable = false)
    private Integer sequence;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    protected MissionWaypoint() {
    }

    public MissionWaypoint(Integer sequence, BigDecimal latitude, BigDecimal longitude) {
        this.sequence = sequence;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getSequence() {
        return sequence;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
