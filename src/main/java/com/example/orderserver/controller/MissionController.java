package com.example.orderserver.controller;

import com.example.orderserver.dto.AssignDeliveryMissionRequest;
import com.example.orderserver.dto.CompleteDeliveryRequest;
import com.example.orderserver.dto.DeliveryCompletionResponse;
import com.example.orderserver.dto.MissionDispatchResponse;
import com.example.orderserver.dto.MissionResponse;
import com.example.orderserver.dto.RobotMissionEventRequest;
import com.example.orderserver.dto.RobotResponse;
import com.example.orderserver.service.DeliveryCompletionResult;
import com.example.orderserver.service.MissionDispatchResult;
import com.example.orderserver.service.MissionService;
import com.example.orderserver.service.OrderRealtimeService;
import com.example.orderserver.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class MissionController {

    private final MissionService missionService;
    private final OrderService orderService;
    private final OrderRealtimeService orderRealtimeService;

    public MissionController(
            MissionService missionService,
            OrderService orderService,
            OrderRealtimeService orderRealtimeService
    ) {
        this.missionService = missionService;
        this.orderService = orderService;
        this.orderRealtimeService = orderRealtimeService;
    }

    @PostMapping("/api/orders/{orderId}/assign-robot")
    public MissionDispatchResponse assignDeliveryMission(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) AssignDeliveryMissionRequest request
    ) {
        MissionDispatchResult result = missionService.assignDeliveryMission(orderId, request);
        orderRealtimeService.publishOrderUpdate(OrderMapper.toResponse(orderService.getOrder(orderId)));
        return new MissionDispatchResponse(
                MissionMapper.toResponse(result.mission()),
                result.robotConnected(),
                result.dispatchMessage()
        );
    }

    @PostMapping("/api/orders/{orderId}/received")
    public DeliveryCompletionResponse completeDelivery(
            @PathVariable UUID orderId,
            @Valid @RequestBody(required = false) CompleteDeliveryRequest request
    ) {
        DeliveryCompletionResult result = missionService.completeDelivery(orderId, request);
        orderRealtimeService.publishOrderUpdate(OrderMapper.toResponse(orderService.getOrder(orderId)));
        return new DeliveryCompletionResponse(
                MissionMapper.toResponse(result.deliveryMission()),
                MissionMapper.toResponse(result.returnMission()),
                result.robotConnected(),
                result.returnMissionCreated(),
                result.dispatchMessage()
        );
    }

    @GetMapping("/api/missions")
    public List<MissionResponse> getMissions(
            @RequestParam(value = "orderId", required = false) UUID orderId
    ) {
        return missionService.getMissions(orderId).stream()
                .map(MissionMapper::toResponse)
                .toList();
    }

    @GetMapping("/api/missions/{missionId}")
    public MissionResponse getMission(@PathVariable UUID missionId) {
        return MissionMapper.toResponse(missionService.getMission(missionId));
    }

    @PostMapping("/api/robot/missions/events")
    public MissionResponse recordMissionEvent(@Valid @RequestBody RobotMissionEventRequest request) {
        return MissionMapper.toResponse(missionService.handleRobotEvent(request));
    }

    @GetMapping("/api/robot/status")
    public RobotResponse getRobotStatus() {
        return MissionMapper.toResponse(missionService.getDefaultRobot());
    }
}
