package com.example.orderserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "robot_route_events")
public class RobotRouteEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false, length = 100)
    private String commandId;

    @Column(name = "robot_id", nullable = false, length = 50)
    private String eventSource = Robot.SINGLE_ROBOT_KEY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RobotRouteEventStatus status;

    @Column(nullable = false)
    private Integer currentWaypointSequence;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    protected RobotRouteEvent() {
    }

    private RobotRouteEvent(
            String commandId,
            RobotRouteEventStatus status,
            Integer currentWaypointSequence,
            BigDecimal latitude,
            BigDecimal longitude,
            String message
    ) {
        this.commandId = commandId;
        this.status = status;
        this.currentWaypointSequence = currentWaypointSequence;
        this.latitude = latitude;
        this.longitude = longitude;
        this.message = message;
    }

    public static RobotRouteEvent create(
            String commandId,
            RobotRouteEventStatus status,
            Integer currentWaypointSequence,
            BigDecimal latitude,
            BigDecimal longitude,
            String message
    ) {
        return new RobotRouteEvent(
                commandId,
                status,
                currentWaypointSequence,
                latitude,
                longitude,
                message
        );
    }

    @PrePersist
    void onCreate() {
        this.receivedAt = LocalDateTime.now();
    }

    public Long getEventId() {
        return eventId;
    }

    public String getCommandId() {
        return commandId;
    }

    public RobotRouteEventStatus getStatus() {
        return status;
    }

    public Integer getCurrentWaypointSequence() {
        return currentWaypointSequence;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}
