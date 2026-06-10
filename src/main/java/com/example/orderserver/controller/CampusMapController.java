package com.example.orderserver.controller;

import com.example.orderserver.dto.CampusMapResponse;
import com.example.orderserver.dto.CampusRouteResponse;
import com.example.orderserver.service.CampusMapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campus-map")
public class CampusMapController {

    private final CampusMapService campusMapService;

    public CampusMapController(CampusMapService campusMapService) {
        this.campusMapService = campusMapService;
    }

    @GetMapping
    public CampusMapResponse getCampusMap() {
        return CampusMapMapper.toMapResponse(
                campusMapService.getNodes(),
                campusMapService.getEdges()
        );
    }

    @GetMapping("/routes")
    public CampusRouteResponse findRoute(
            @RequestParam("from") String fromNodeId,
            @RequestParam("to") String toNodeId
    ) {
        return CampusMapMapper.toResponse(campusMapService.findRoute(fromNodeId, toNodeId));
    }
}
