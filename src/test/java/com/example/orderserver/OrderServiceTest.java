package com.example.orderserver;

import com.example.orderserver.domain.OrderStatus;
import com.example.orderserver.dto.CreateOrderItemRequest;
import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.dto.UpdateOrderRequest;
import com.example.orderserver.exception.InvalidOrderStatusTransitionException;
import com.example.orderserver.repository.OrderRepository;
import com.example.orderserver.repository.RobotDispatchRepository;
import com.example.orderserver.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RobotDispatchRepository robotDispatchRepository;

    @BeforeEach
    void setUp() {
        robotDispatchRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void adminOrderShouldBeSentToRobot() {
        var order = orderService.createAdminOrder(sampleCreateRequest());

        assertTrue(order.isSentToRobot());
        assertEquals(1, robotDispatchRepository.findAll().size());
    }

    @Test
    void orderShouldBeUpdatedInDatabase() {
        var order = orderService.createMobileOrder(sampleCreateRequest());

        var updated = orderService.updateOrder(
                order.getId(),
                new UpdateOrderRequest(
                        "Updated Customer",
                        "010-2222-3333",
                        "Busan",
                        List.of(new CreateOrderItemRequest("Latte", 1, BigDecimal.valueOf(5200)))
                )
        );

        assertEquals("Updated Customer", updated.getCustomerName());
        assertEquals(1, updated.getItems().size());
        assertEquals("Latte", updated.getItems().get(0).name());
    }

    @Test
    void statusShouldFollowDefinedSequence() {
        var order = orderService.createMobileOrder(sampleCreateRequest());

        orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERING);
        var updated = orderService.updateOrderStatus(order.getId(), OrderStatus.COMPLETED);

        assertEquals(OrderStatus.COMPLETED, updated.getStatus());
    }

    @Test
    void statusShouldBeAbleToMoveBackByOneStep() {
        var order = orderService.createMobileOrder(sampleCreateRequest());

        orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERING);
        orderService.updateOrderStatus(order.getId(), OrderStatus.COMPLETED);
        var reverted = orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERING);

        assertEquals(OrderStatus.DELIVERING, reverted.getStatus());
    }

    @Test
    void invalidStatusJumpShouldFail() {
        var order = orderService.createMobileOrder(sampleCreateRequest());

        assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> orderService.updateOrderStatus(order.getId(), OrderStatus.COMPLETED)
        );
    }

    @Test
    void orderShouldBeDeletedFromDatabase() {
        var order = orderService.createMobileOrder(sampleCreateRequest());

        orderService.deleteOrder(order.getId());

        assertTrue(orderRepository.findById(order.getId()).isEmpty());
    }

    private CreateOrderRequest sampleRequest() {
        return new CreateOrderRequest(
                "홍길동",
                "010-0000-0000",
                "서울시 중구 세종대로 1",
                List.of(new CreateOrderItemRequest("샌드위치", 2, BigDecimal.valueOf(5500)))
        );
    }
    private CreateOrderRequest sampleCreateRequest() {
        return new CreateOrderRequest(
                "Alice",
                "010-0000-0000",
                "Seoul",
                List.of(new CreateOrderItemRequest("Sandwich", 2, BigDecimal.valueOf(5500)))
        );
    }
}
