package com.example.orderserver.controller;

import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.dto.OrderResponse;
import com.example.orderserver.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class MobileOrderController {

    private final OrderService orderService;

    public MobileOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return OrderMapper.toResponse(orderService.createMobileOrder(request));
    }
}
