package com.example.orderserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "robot_dispatches")
public class RobotDispatch {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID dispatchId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(name = "target_robot", nullable = false, length = 50)
    private String dispatchTarget = Robot.SINGLE_ROBOT_KEY;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private LocalDateTime dispatchedAt;

    protected RobotDispatch() {
    }

    private RobotDispatch(UUID dispatchId, UUID orderId, String message) {
        this.dispatchId = dispatchId;
        this.orderId = orderId;
        this.message = message;
    }

    public static RobotDispatch create(UUID dispatchId, UUID orderId, String message) {
        return new RobotDispatch(dispatchId, orderId, message);
    }

    @PrePersist
    void onCreate() {
        this.dispatchedAt = LocalDateTime.now();
    }

    public UUID getDispatchId() {
        return dispatchId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }
}
