package com.example.orderserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "robots")
public class Robot {

    public static final String SINGLE_ROBOT_KEY = "SINGLE_ROBOT";

    @Id
    @Column(nullable = false, updatable = false, length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RobotStatus status;

    private UUID currentMissionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Robot() {
    }

    private Robot(String id, RobotStatus status) {
        this.id = id;
        this.status = status;
    }

    public static Robot createDefault() {
        return new Robot(SINGLE_ROBOT_KEY, RobotStatus.IDLE);
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public RobotStatus getStatus() {
        return status;
    }

    public UUID getCurrentMissionId() {
        return currentMissionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean canAcceptDelivery() {
        return status == RobotStatus.IDLE;
    }

    public void assignDelivery(UUID missionId) {
        this.status = RobotStatus.DELIVERY_ASSIGNED;
        this.currentMissionId = missionId;
    }

    public void markBusy(UUID missionId) {
        this.status = RobotStatus.BUSY;
        this.currentMissionId = missionId;
    }

    public void startReturn(UUID missionId) {
        this.status = RobotStatus.RETURNING;
        this.currentMissionId = missionId;
    }

    public void markIdle() {
        this.status = RobotStatus.IDLE;
        this.currentMissionId = null;
    }
}
