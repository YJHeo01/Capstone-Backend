package com.example.orderserver.controller;

import com.example.orderserver.dto.RobotDispatchResponse;
import com.example.orderserver.service.RobotGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/robot/dispatches")
public class RobotController {

    private final RobotGateway robotGateway;

    public RobotController(RobotGateway robotGateway) {
        this.robotGateway = robotGateway;
    }

    @GetMapping
    public List<RobotDispatchResponse> getDispatchHistory() {
        return robotGateway.getDispatchHistory().stream()
                .map(OrderMapper::toResponse)
                .toList();
    }
}
