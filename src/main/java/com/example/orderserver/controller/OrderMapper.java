package com.example.orderserver.controller;

import com.example.orderserver.domain.Order;
import com.example.orderserver.domain.OrderItem;
import com.example.orderserver.domain.RobotDispatch;
import com.example.orderserver.domain.RobotLocation;
import com.example.orderserver.dto.OrderItemResponse;
import com.example.orderserver.dto.OrderResponse;
import com.example.orderserver.dto.OrderStatusResponse;
import com.example.orderserver.dto.RobotDispatchResponse;
import com.example.orderserver.dto.RobotLocationResponse;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getSource(),
                order.getCustomerName(),
                order.getPhoneNumber(),
                order.getDeliveryAddress(),
                order.getItems().stream().map(OrderMapper::toItemResponse).toList(),
                order.totalPrice(),
                order.getStatus(),
                order.isSentToRobot(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public static RobotDispatchResponse toResponse(RobotDispatch dispatch) {
        return new RobotDispatchResponse(
                dispatch.getDispatchId(),
                dispatch.getOrderId(),
                dispatch.getTargetRobot(),
                dispatch.getMessage(),
                dispatch.getDispatchedAt()
        );
    }

    public static RobotLocationResponse toResponse(RobotLocation location) {
        return new RobotLocationResponse(
                location.getLatitude(),
                location.getLongitude(),
                location.getUpdatedAt()
        );
    }

    public static OrderStatusResponse toStatusResponse(Order order) {
        return toStatusResponse(order, null);
    }

    public static OrderStatusResponse toStatusResponse(Order order, RobotLocation robotLocation) {
        return new OrderStatusResponse(
                order.getId(),
                order.getStatus(),
                order.isSentToRobot(),
                order.getUpdatedAt(),
                robotLocation == null ? null : toResponse(robotLocation)
        );
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.name(),
                item.quantity(),
                item.unitPrice(),
                item.totalPrice()
        );
    }
}
