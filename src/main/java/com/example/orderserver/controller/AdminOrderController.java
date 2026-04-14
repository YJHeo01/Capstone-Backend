package com.example.orderserver.controller;

import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.dto.OrderResponse;
import com.example.orderserver.dto.UpdateOrderRequest;
import com.example.orderserver.dto.UpdateOrderStatusRequest;
import com.example.orderserver.service.OrderRealtimeService;
import com.example.orderserver.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderRealtimeService orderRealtimeService;

    public AdminOrderController(OrderService orderService, OrderRealtimeService orderRealtimeService) {
        this.orderService = orderService;
        this.orderRealtimeService = orderRealtimeService;
    }

    @GetMapping
    public List<OrderResponse> getOrders() {
        return orderService.getAllOrders().stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable UUID orderId) {
        return OrderMapper.toResponse(orderService.getOrder(orderId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createAdminOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = OrderMapper.toResponse(orderService.createAdminOrder(request));
        orderRealtimeService.publishOrderUpdate(response);
        return response;
    }

    @PutMapping("/{orderId}")
    public OrderResponse updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderRequest request
    ) {
        OrderResponse response = OrderMapper.toResponse(orderService.updateOrder(orderId, request));
        orderRealtimeService.publishOrderUpdate(response);
        return response;
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse response = OrderMapper.toResponse(orderService.updateOrderStatus(orderId, request.status()));
        orderRealtimeService.publishOrderUpdate(response);
        return response;
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
        orderRealtimeService.publishOrderDeleted(orderId);
    }
}
