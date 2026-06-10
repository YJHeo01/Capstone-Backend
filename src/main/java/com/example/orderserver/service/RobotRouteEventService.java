package com.example.orderserver.service;

import com.example.orderserver.domain.RobotRouteEvent;
import com.example.orderserver.domain.RobotRouteEventStatus;
import com.example.orderserver.dto.RobotLocationRequest;
import com.example.orderserver.dto.RobotMissionEventRequest;
import com.example.orderserver.dto.RobotMissionEventType;
import com.example.orderserver.dto.RobotRouteEventRequest;
import com.example.orderserver.repository.RobotRouteEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RobotRouteEventService {

    private final RobotRouteEventRepository robotRouteEventRepository;
    private final RobotLocationService robotLocationService;
    private final MissionService missionService;

    public RobotRouteEventService(
            RobotRouteEventRepository robotRouteEventRepository,
            RobotLocationService robotLocationService,
            MissionService missionService
    ) {
        this.robotRouteEventRepository = robotRouteEventRepository;
        this.robotLocationService = robotLocationService;
        this.missionService = missionService;
    }

    @Transactional
    public RobotRouteEvent recordRouteEvent(RobotRouteEventRequest request) {
        RobotRouteEvent event = RobotRouteEvent.create(
                request.commandId(),
                request.status(),
                request.currentWaypointSequence(),
                request.latitude(),
                request.longitude(),
                request.message()
        );

        robotLocationService.saveLatestLocation(
                new RobotLocationRequest(request.latitude(), request.longitude())
        );
        updateMissionStatusIfCommandIdIsMissionId(request);

        return robotRouteEventRepository.save(event);
    }

    public List<RobotRouteEvent> getRouteEvents(String commandId) {
        if (StringUtils.hasText(commandId)) {
            return robotRouteEventRepository.findAllByCommandIdOrderByReceivedAtAsc(commandId);
        }
        return robotRouteEventRepository.findAllByOrderByReceivedAtDesc();
    }

    private void updateMissionStatusIfCommandIdIsMissionId(RobotRouteEventRequest request) {
        try {
            UUID missionId = UUID.fromString(request.commandId());
            missionService.handleRobotEvent(new RobotMissionEventRequest(
                    missionId,
                    toMissionEventType(request.status()),
                    request.currentWaypointSequence(),
                    request.latitude(),
                    request.longitude(),
                    request.message()
            ));
        } catch (IllegalArgumentException ignored) {
            // Existing route-event clients may use free-form command IDs.
        }
    }

    private RobotMissionEventType toMissionEventType(RobotRouteEventStatus status) {
        return switch (status) {
            case ACCEPTED -> RobotMissionEventType.MISSION_ACK;
            case MOVING, WAYPOINT_REACHED -> RobotMissionEventType.MISSION_STARTED;
            case ARRIVED -> RobotMissionEventType.ARRIVED_AT_DESTINATION;
            case FAILED, CANCELED -> RobotMissionEventType.MISSION_FAILED;
        };
    }
}
