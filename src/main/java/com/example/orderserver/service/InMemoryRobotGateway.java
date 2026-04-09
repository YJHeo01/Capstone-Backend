package com.example.orderserver.service;

import com.example.orderserver.domain.Order;
import com.example.orderserver.domain.RobotDispatch;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryRobotGateway implements RobotGateway {

    private static final String DEFAULT_ROBOT_NAME = "DELIVERY_ROBOT_01";

    private final CopyOnWriteArrayList<RobotDispatch> dispatches = new CopyOnWriteArrayList<>();

    @Override
    public RobotDispatch sendOrder(Order order) {
        RobotDispatch dispatch = RobotDispatch.create(
                UUID.randomUUID(),
                order.getId(),
                DEFAULT_ROBOT_NAME,
                buildMessage(order)
        );
        dispatches.add(dispatch);
        return dispatch;
    }

    @Override
    public List<RobotDispatch> getDispatchHistory() {
        return dispatches.stream()
                .sorted(Comparator.comparing(RobotDispatch::getDispatchedAt).reversed())
                .toList();
    }

    private String buildMessage(Order order) {
        return "관리자 주문 전달: 주문번호=%s, 주문자=%s, 주소=%s, 총금액=%s"
                .formatted(order.getId(), order.getCustomerName(), order.getDeliveryAddress(), order.totalPrice());
    }
}
