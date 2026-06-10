package com.example.orderserver.service;

import com.example.orderserver.domain.CampusPoint;
import com.example.orderserver.domain.CampusRoute;
import com.example.orderserver.domain.Mission;
import com.example.orderserver.domain.MissionStatus;
import com.example.orderserver.domain.MissionType;
import com.example.orderserver.domain.MissionWaypoint;
import com.example.orderserver.domain.Order;
import com.example.orderserver.domain.OrderStatus;
import com.example.orderserver.domain.Robot;
import com.example.orderserver.dto.AssignDeliveryMissionRequest;
import com.example.orderserver.dto.CompleteDeliveryRequest;
import com.example.orderserver.dto.RobotLocationRequest;
import com.example.orderserver.dto.RobotMissionEventRequest;
import com.example.orderserver.dto.RobotMissionEventType;
import com.example.orderserver.exception.InvalidMissionStateException;
import com.example.orderserver.exception.MissionNotFoundException;
import com.example.orderserver.exception.OrderNotFoundException;
import com.example.orderserver.exception.RobotUnavailableException;
import com.example.orderserver.repository.MissionRepository;
import com.example.orderserver.repository.OrderRepository;
import com.example.orderserver.repository.RobotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional(readOnly = true)
public class MissionService {

    public static final String BASE_NODE_ID = "info_a";
    public static final String DEFAULT_DELIVERY_DESTINATION_NODE_ID = "social_science_front";

    private final OrderRepository orderRepository;
    private final MissionRepository missionRepository;
    private final RobotRepository robotRepository;
    private final RouteService routeService;
    private final RobotCommandGateway robotCommandGateway;
    private final RobotLocationService robotLocationService;
    private final MissionRouteDispatcher missionRouteDispatcher;

    public MissionService(
            OrderRepository orderRepository,
            MissionRepository missionRepository,
            RobotRepository robotRepository,
            RouteService routeService,
            RobotCommandGateway robotCommandGateway,
            RobotLocationService robotLocationService,
            MissionRouteDispatcher missionRouteDispatcher
    ) {
        this.orderRepository = orderRepository;
        this.missionRepository = missionRepository;
        this.robotRepository = robotRepository;
        this.routeService = routeService;
        this.robotCommandGateway = robotCommandGateway;
        this.robotLocationService = robotLocationService;
        this.missionRouteDispatcher = missionRouteDispatcher;
    }

    @Transactional
    public MissionDispatchResult assignDeliveryMission(UUID orderId, AssignDeliveryMissionRequest request) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.WAITING) {
            throw new InvalidMissionStateException(
                    "Only waiting orders can be assigned to a robot. Current status: " + order.getStatus()
            );
        }

        Robot robot = getDefaultRobot();
        if (!robot.canAcceptDelivery()) {
            throw new RobotUnavailableException(
                    "Robot cannot accept a new delivery while status is %s."
                            .formatted(robot.getStatus())
            );
        }

        String fromNodeId = resolveFromNodeId(request);
        String destinationNodeId = resolveDestinationNodeId(order, request);
        CampusRoute route = routeService.findRoute(fromNodeId, destinationNodeId);
        Mission mission = Mission.create(
                order.getId(),
                MissionType.DELIVERY,
                fromNodeId,
                destinationNodeId,
                toWaypoints(route)
        );

        Mission savedMission = missionRepository.save(mission);
        robot.assignDelivery(savedMission.getMissionId());
        robotRepository.save(robot);

        order.updateStatus(OrderStatus.DELIVERING);
        order.markSentToRobot();
        orderRepository.save(order);

        return scheduleMissionDispatch(savedMission);
    }

    @Transactional
    public DeliveryCompletionResult completeDelivery(UUID orderId, CompleteDeliveryRequest request) {
        Order order = getOrder(orderId);
        Mission deliveryMission = missionRepository
                .findFirstByOrderIdAndTypeOrderByCreatedAtDesc(orderId, MissionType.DELIVERY)
                .orElseThrow(() -> new InvalidMissionStateException("Delivery mission does not exist for order: " + orderId));

        Robot robot = getDefaultRobot();
        Mission existingReturnMission = missionRepository
                .findFirstByOrderIdAndTypeOrderByCreatedAtDesc(orderId, MissionType.RETURN)
                .orElse(null);

        if (existingReturnMission != null) {
            return new DeliveryCompletionResult(
                    deliveryMission,
                    existingReturnMission,
                    robotCommandGateway.isConnected(),
                    false,
                    "Return mission already exists."
            );
        }

        if (deliveryMission.getStatus() != MissionStatus.ARRIVED && deliveryMission.getStatus() != MissionStatus.COMPLETED) {
            throw new InvalidMissionStateException(
                    "Delivery mission must be ARRIVED before completing pickup. Current status: "
                            + deliveryMission.getStatus()
            );
        }

        if (!deliveryMission.isCompleted()) {
            deliveryMission.markCompleted(request == null ? null : request.message());
            missionRepository.save(deliveryMission);
        }

        order.updateStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        Mission returnMission = createReturnMission(orderId, deliveryMission.getToNodeId());
        Mission savedReturnMission = missionRepository.save(returnMission);
        robot.startReturn(savedReturnMission.getMissionId());
        robotRepository.save(robot);

        MissionDispatchResult dispatchResult = scheduleMissionDispatch(savedReturnMission);
        return new DeliveryCompletionResult(
                deliveryMission,
                dispatchResult.mission(),
                dispatchResult.robotConnected(),
                true,
                dispatchResult.dispatchMessage()
        );
    }

    @Transactional
    public Mission handleRobotEvent(RobotMissionEventRequest request) {
        Mission mission = getMission(request.missionId());
        Robot robot = getDefaultRobot();
        saveLocationIfPresent(request);

        RobotMissionEventType eventType = request.eventType();
        switch (eventType) {
            case MISSION_ACK -> {
                mission.markAcked(request.message());
                if (mission.getType() == MissionType.DELIVERY) {
                    robot.markBusy(mission.getMissionId());
                } else {
                    robot.startReturn(mission.getMissionId());
                }
            }
            case MISSION_STARTED -> {
                mission.markInProgress(request.message());
                if (mission.getType() == MissionType.DELIVERY) {
                    robot.markBusy(mission.getMissionId());
                } else {
                    robot.startReturn(mission.getMissionId());
                }
            }
            case ARRIVED_AT_DESTINATION -> mission.markArrived(request.message());
            case RETURN_COMPLETED -> {
                if (mission.getType() != MissionType.RETURN) {
                    throw new InvalidMissionStateException("RETURN_COMPLETED can only be applied to RETURN missions.");
                }
                mission.markCompleted(request.message());
                robot.markIdle();
            }
            case MISSION_FAILED -> mission.markFailed(request.message());
        }

        robotRepository.save(robot);
        return missionRepository.save(mission);
    }

    public Mission getMission(UUID missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException(missionId));
    }

    public List<Mission> getMissions(UUID orderId) {
        if (orderId != null) {
            return missionRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId);
        }
        return missionRepository.findAll();
    }

    @Transactional
    public Robot getDefaultRobot() {
        return robotRepository.findById(Robot.SINGLE_ROBOT_KEY)
                .orElseGet(() -> robotRepository.save(Robot.createDefault()));
    }

    @Transactional
    public void dispatchPendingMissions() {
        List<Mission> pendingMissions = missionRepository.findAllByStatusInOrderByCreatedAtAsc(List.of(MissionStatus.CREATED));
        for (Mission mission : pendingMissions) {
            scheduleMissionDispatch(mission);
        }
    }

    private Mission createReturnMission(UUID orderId, String fromNodeId) {
        CampusRoute route = routeService.findRoute(fromNodeId, BASE_NODE_ID);
        return Mission.create(
                orderId,
                MissionType.RETURN,
                fromNodeId,
                BASE_NODE_ID,
                toWaypoints(route)
        );
    }

    private MissionDispatchResult scheduleMissionDispatch(Mission mission) {
        boolean robotConnected = robotCommandGateway.isConnected();
        if (robotConnected) {
            dispatchAfterCommit(mission.getMissionId());
            return new MissionDispatchResult(mission, true, "Mission saved. Route will be sent after commit.");
        }
        return new MissionDispatchResult(mission, false, "Mission saved, but robot is not connected.");
    }

    private void dispatchAfterCommit(UUID missionId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            missionRouteDispatcher.dispatch(missionId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                missionRouteDispatcher.dispatch(missionId);
            }
        });
    }

    private Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private String resolveFromNodeId(AssignDeliveryMissionRequest request) {
        if (request != null && StringUtils.hasText(request.fromNodeId())) {
            return request.fromNodeId();
        }
        return BASE_NODE_ID;
    }

    private String resolveDestinationNodeId(Order order, AssignDeliveryMissionRequest request) {
        if (request != null && StringUtils.hasText(request.destinationNodeId())) {
            return request.destinationNodeId();
        }
        if (routeService.hasNode(order.getDeliveryAddress())) {
            return order.getDeliveryAddress();
        }
        return DEFAULT_DELIVERY_DESTINATION_NODE_ID;
    }

    private List<MissionWaypoint> toWaypoints(CampusRoute route) {
        AtomicInteger sequence = new AtomicInteger(1);
        return route.waypoints().stream()
                .map(point -> toWaypoint(sequence.getAndIncrement(), point))
                .toList();
    }

    private MissionWaypoint toWaypoint(int sequence, CampusPoint point) {
        return new MissionWaypoint(sequence, point.latitude(), point.longitude());
    }

    private void saveLocationIfPresent(RobotMissionEventRequest request) {
        if (request.latitude() == null || request.longitude() == null) {
            return;
        }
        robotLocationService.saveLatestLocation(new RobotLocationRequest(request.latitude(), request.longitude()));
    }
}
