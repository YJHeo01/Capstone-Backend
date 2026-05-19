package com.example.orderserver.controller;

import com.example.orderserver.dto.RobotLocationRequest;
import com.example.orderserver.dto.RobotLocationResponse;
import com.example.orderserver.service.RobotLocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/robot/location")
public class RobotLocationController {

    private final RobotLocationService robotLocationService;

    public RobotLocationController(RobotLocationService robotLocationService) {
        this.robotLocationService = robotLocationService;
    }

    @PostMapping
    public RobotLocationResponse updateLocation(@Valid @RequestBody RobotLocationRequest request) {
        return OrderMapper.toResponse(robotLocationService.saveLatestLocation(request));
    }

    @GetMapping
    public ResponseEntity<RobotLocationResponse> getLatestLocation() {
        return robotLocationService.getLatestLocation()
                .map(OrderMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
