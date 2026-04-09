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
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderSource source;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 50)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String deliveryAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "item_position")
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean sentToRobot;

    protected Order() {
    }

    private Order(
            UUID id,
            OrderSource source,
            String customerName,
            String phoneNumber,
            String deliveryAddress,
            List<OrderItem> items,
            OrderStatus status,
            boolean sentToRobot
    ) {
        this.id = id;
        this.source = source;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.deliveryAddress = deliveryAddress;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.sentToRobot = sentToRobot;
    }

    public static Order create(OrderSource source, String customerName, String phoneNumber, String deliveryAddress, List<OrderItem> items) {
        return new Order(
                UUID.randomUUID(),
                source,
                customerName,
                phoneNumber,
                deliveryAddress,
                items,
                OrderStatus.WAITING,
                false
        );
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

    public UUID getId() {
        return id;
    }

    public OrderSource getSource() {
        return source;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isSentToRobot() {
        return sentToRobot;
    }

    public void updateDetails(String customerName, String phoneNumber, String deliveryAddress, List<OrderItem> items) {
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.deliveryAddress = deliveryAddress;
        this.items.clear();
        this.items.addAll(items);
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void markSentToRobot() {
        this.sentToRobot = true;
    }

    public BigDecimal totalPrice() {
        return items.stream()
                .map(OrderItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
