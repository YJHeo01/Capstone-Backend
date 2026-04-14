package com.example.orderserver;

import com.example.orderserver.dto.CreateOrderItemRequest;
import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.repository.OrderRepository;
import com.example.orderserver.repository.RobotDispatchRepository;
import com.example.orderserver.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MobileOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.example.orderserver.controller.MobileOrderController mobileOrderController;

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
    void mobileClientShouldFetchSingleOrder() throws Exception {
        var order = orderService.createMobileOrder(sampleRequest());

        mockMvc.perform(get("/api/orders/{orderId}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId().toString()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.source").value("MOBILE_APP"));
    }

    @Test
    void mobileClientShouldPollOrderStatus() throws Exception {
        var order = orderService.createMobileOrder(sampleRequest());

        mockMvc.perform(get("/api/orders/{orderId}/status", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.sentToRobot").value(false));
    }

    @Test
    void mobileClientShouldSubscribeToRealtimeOrderUpdates() {
        var order = orderService.createMobileOrder(sampleRequest());

        SseEmitter emitter = mobileOrderController.streamOrder(order.getId());

        assertNotNull(emitter);
    }

    private CreateOrderRequest sampleRequest() {
        return new CreateOrderRequest(
                "Alice",
                "010-0000-0000",
                "Seoul",
                List.of(new CreateOrderItemRequest("Sandwich", 2, BigDecimal.valueOf(5500)))
        );
    }
}
