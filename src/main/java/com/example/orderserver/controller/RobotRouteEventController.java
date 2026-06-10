package com.example.orderserver.controller;

import com.example.orderserver.dto.RobotRouteEventRequest;
import com.example.orderserver.dto.RobotRouteEventResponse;
import com.example.orderserver.service.RobotRouteEventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/robot/route-events")
public class RobotRouteEventController {

    private final RobotRouteEventService robotRouteEventService;

    public RobotRouteEventController(RobotRouteEventService robotRouteEventService) {
        this.robotRouteEventService = robotRouteEventService;
    }

    @PostMapping
    public RobotRouteEventResponse recordRouteEvent(@Valid @RequestBody RobotRouteEventRequest request) {
        return RobotRouteEventMapper.toResponse(robotRouteEventService.recordRouteEvent(request));
    }

    @GetMapping
    public List<RobotRouteEventResponse> getRouteEvents(
            @RequestParam(value = "commandId", required = false) String commandId
    ) {
        return robotRouteEventService.getRouteEvents(commandId).stream()
                .map(RobotRouteEventMapper::toResponse)
                .toList();
    }
}
