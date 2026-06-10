package com.example.orderserver.controller;

import com.example.orderserver.dto.AssignDeliveryMissionRequest;
import com.example.orderserver.dto.CreateOrderItemRequest;
import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.dto.MissionDispatchResponse;
import com.example.orderserver.service.MissionDispatchResult;
import com.example.orderserver.service.MissionService;
import com.example.orderserver.service.OrderRealtimeService;
import com.example.orderserver.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/robot/test-routes")
public class RobotTestRouteController {

    private final OrderService orderService;
    private final MissionService missionService;
    private final OrderRealtimeService orderRealtimeService;

    public RobotTestRouteController(
            OrderService orderService,
            MissionService missionService,
            OrderRealtimeService orderRealtimeService
    ) {
        this.orderService = orderService;
        this.missionService = missionService;
        this.orderRealtimeService = orderRealtimeService;
    }

    @PostMapping("/delivery")
    @Transactional
    public MissionDispatchResponse sendVirtualDeliveryRoute(
            @Valid @RequestBody(required = false) AssignDeliveryMissionRequest request
    ) {
        AssignDeliveryMissionRequest routeRequest = request == null
                ? new AssignDeliveryMissionRequest(null, null)
                : request;
        String destinationNodeId = StringUtils.hasText(routeRequest.destinationNodeId())
                ? routeRequest.destinationNodeId()
                : MissionService.DEFAULT_DELIVERY_DESTINATION_NODE_ID;

        var order = orderService.createMobileOrder(new CreateOrderRequest(
                "Robot Test User",
                "010-0000-0000",
                destinationNodeId,
                List.of(new CreateOrderItemRequest("Demo Item", 1, BigDecimal.ZERO))
        ));

        MissionDispatchResult result = missionService.assignDeliveryMission(order.getId(), routeRequest);
        orderRealtimeService.publishOrderUpdate(OrderMapper.toResponse(orderService.getOrder(order.getId())));
        return new MissionDispatchResponse(
                MissionMapper.toResponse(result.mission()),
                result.robotConnected(),
                result.dispatchMessage()
        );
    }
}
