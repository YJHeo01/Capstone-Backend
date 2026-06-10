package com.example.orderserver.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "missions")
public class Mission {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID missionId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(name = "robot_id", nullable = false, length = 50)
    private String assignedRobot = Robot.SINGLE_ROBOT_KEY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MissionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MissionStatus status;

    @Column(nullable = false, length = 100)
    private String fromNodeId;

    @Column(nullable = false, length = 100)
    private String toNodeId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mission_waypoints", joinColumns = @JoinColumn(name = "mission_id"))
    @OrderBy("sequence ASC")
    private List<MissionWaypoint> waypoints = new ArrayList<>();

    @Column(length = 500)
    private String lastMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime dispatchedAt;

    private LocalDateTime ackedAt;

    private LocalDateTime startedAt;

    private LocalDateTime arrivedAt;

    private LocalDateTime completedAt;

    protected Mission() {
    }

    private Mission(
            UUID missionId,
            UUID orderId,
            MissionType type,
            String fromNodeId,
            String toNodeId,
            List<MissionWaypoint> waypoints
    ) {
        this.missionId = missionId;
        this.orderId = orderId;
        this.type = type;
        this.status = MissionStatus.CREATED;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.waypoints = new ArrayList<>(waypoints);
    }

    public static Mission create(
            UUID orderId,
            MissionType type,
            String fromNodeId,
            String toNodeId,
            List<MissionWaypoint> waypoints
    ) {
        return new Mission(UUID.randomUUID(), orderId, type, fromNodeId, toNodeId, waypoints);
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

    public UUID getMissionId() {
        return missionId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public MissionType getType() {
        return type;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public List<MissionWaypoint> getWaypoints() {
        return List.copyOf(waypoints);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public LocalDateTime getAckedAt() {
        return ackedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getArrivedAt() {
        return arrivedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public boolean isCompleted() {
        return status == MissionStatus.COMPLETED;
    }

    public void markDispatched() {
        this.status = MissionStatus.DISPATCHED;
        this.dispatchedAt = LocalDateTime.now();
    }

    public void markAcked(String message) {
        this.status = MissionStatus.ACKED;
        this.ackedAt = LocalDateTime.now();
        this.lastMessage = message;
    }

    public void markInProgress(String message) {
        this.status = MissionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.lastMessage = message;
    }

    public void markArrived(String message) {
        this.status = MissionStatus.ARRIVED;
        this.arrivedAt = LocalDateTime.now();
        this.lastMessage = message;
    }

    public void markCompleted(String message) {
        this.status = MissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.lastMessage = message;
    }

    public void markFailed(String message) {
        this.status = MissionStatus.FAILED;
        this.lastMessage = message;
    }
}
