package com.example.orderserver.service;

import com.example.orderserver.domain.Order;
import com.example.orderserver.domain.OrderItem;
import com.example.orderserver.domain.OrderSource;
import com.example.orderserver.domain.OrderStatus;
import com.example.orderserver.dto.CreateOrderItemRequest;
import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.dto.UpdateOrderRequest;
import com.example.orderserver.exception.InvalidOrderStatusTransitionException;
import com.example.orderserver.exception.OrderNotFoundException;
import com.example.orderserver.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final RobotGateway robotGateway;

    public OrderService(OrderRepository orderRepository, RobotGateway robotGateway) {
        this.orderRepository = orderRepository;
        this.robotGateway = robotGateway;
    }

    @Transactional
    public Order createMobileOrder(CreateOrderRequest request) {
        return createOrder(OrderSource.MOBILE_APP, request);
    }

    @Transactional
    public Order createAdminOrder(CreateOrderRequest request) {
        Order order = createOrder(OrderSource.ADMIN, request);
        robotGateway.sendOrder(order);
        order.markSentToRobot();
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional
    public Order updateOrder(UUID orderId, UpdateOrderRequest request) {
        Order order = getOrder(orderId);
        order.updateDetails(
                request.customerName(),
                request.phoneNumber(),
                request.deliveryAddress(),
                toOrderItems(request.items())
        );
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = getOrder(orderId);
        validateTransition(order.getStatus(), newStatus);
        order.updateStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(UUID orderId) {
        Order order = getOrder(orderId);
        orderRepository.delete(order);
    }

    private Order createOrder(OrderSource source, CreateOrderRequest request) {
        Order order = Order.create(
                source,
                request.customerName(),
                request.phoneNumber(),
                request.deliveryAddress(),
                toOrderItems(request.items())
        );
        return orderRepository.save(order);
    }

    private List<OrderItem> toOrderItems(List<CreateOrderItemRequest> items) {
        return items.stream()
                .map(this::toOrderItem)
                .toList();
    }

    private OrderItem toOrderItem(CreateOrderItemRequest item) {
        return new OrderItem(item.name(), item.quantity(), item.unitPrice());
    }

    private void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case WAITING -> newStatus == OrderStatus.DELIVERING;
            case DELIVERING -> newStatus == OrderStatus.COMPLETED;
            case COMPLETED -> false;
        };

        if (!valid) {
            throw new InvalidOrderStatusTransitionException(
                    "주문 상태는 %s 에서 %s 로 변경할 수 없습니다.".formatted(currentStatus, newStatus)
            );
        }
    }
}
