package com.example.orderserver.controller;

import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.dto.OrderResponse;
import com.example.orderserver.dto.OrderStatusResponse;
import com.example.orderserver.service.OrderRealtimeService;
import com.example.orderserver.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class MobileOrderController {

    private final OrderService orderService;
    private final OrderRealtimeService orderRealtimeService;

    public MobileOrderController(OrderService orderService, OrderRealtimeService orderRealtimeService) {
        this.orderService = orderService;
        this.orderRealtimeService = orderRealtimeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = OrderMapper.toResponse(orderService.createMobileOrder(request));
        orderRealtimeService.publishOrderUpdate(response);
        return response;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable UUID orderId) {
        return OrderMapper.toResponse(orderService.getOrder(orderId));
    }

    @GetMapping("/{orderId}/status")
    public OrderStatusResponse getOrderStatus(@PathVariable UUID orderId) {
        return OrderMapper.toStatusResponse(orderService.getOrder(orderId));
    }

    @GetMapping(value = "/{orderId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrder(@PathVariable UUID orderId) {
        OrderResponse response = OrderMapper.toResponse(orderService.getOrder(orderId));
        return orderRealtimeService.subscribe(orderId, response);
    }
}
