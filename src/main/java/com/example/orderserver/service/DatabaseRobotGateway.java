package com.example.orderserver.service;

import com.example.orderserver.domain.Order;
import com.example.orderserver.domain.RobotDispatch;
import com.example.orderserver.repository.RobotDispatchRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DatabaseRobotGateway implements RobotGateway {

    private static final String DEFAULT_ROBOT_NAME = "DELIVERY_ROBOT_01";

    private final RobotDispatchRepository robotDispatchRepository;

    public DatabaseRobotGateway(RobotDispatchRepository robotDispatchRepository) {
        this.robotDispatchRepository = robotDispatchRepository;
    }

    @Override
    public RobotDispatch sendOrder(Order order) {
        RobotDispatch dispatch = RobotDispatch.create(
                UUID.randomUUID(),
                order.getId(),
                DEFAULT_ROBOT_NAME,
                buildMessage(order)
        );
        return robotDispatchRepository.save(dispatch);
    }

    @Override
    public List<RobotDispatch> getDispatchHistory() {
        return robotDispatchRepository.findAllByOrderByDispatchedAtDesc();
    }

    private String buildMessage(Order order) {
        return "Admin order dispatch: orderId=%s, customer=%s, address=%s, total=%s"
                .formatted(order.getId(), order.getCustomerName(), order.getDeliveryAddress(), order.totalPrice());
    }
}
