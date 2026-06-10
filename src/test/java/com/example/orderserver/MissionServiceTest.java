package com.example.orderserver;

import com.example.orderserver.domain.MissionStatus;
import com.example.orderserver.domain.MissionType;
import com.example.orderserver.domain.OrderStatus;
import com.example.orderserver.domain.Robot;
import com.example.orderserver.domain.RobotStatus;
import com.example.orderserver.dto.AssignDeliveryMissionRequest;
import com.example.orderserver.dto.CompleteDeliveryRequest;
import com.example.orderserver.dto.CreateOrderItemRequest;
import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.dto.RobotMissionEventRequest;
import com.example.orderserver.dto.RobotMissionEventType;
import com.example.orderserver.exception.RobotUnavailableException;
import com.example.orderserver.repository.MissionRepository;
import com.example.orderserver.repository.OrderRepository;
import com.example.orderserver.repository.RobotRepository;
import com.example.orderserver.service.MissionService;
import com.example.orderserver.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class MissionServiceTest {

    @Autowired
    private MissionService missionService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        missionRepository.deleteAll();
        robotRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void assigningOrderShouldCreateDeliveryMission() {
        var order = orderService.createMobileOrder(sampleCreateRequest());

        var result = missionService.assignDeliveryMission(order.getId(), sampleAssignRequest());

        assertEquals(MissionType.DELIVERY, result.mission().getType());
        assertEquals(MissionStatus.CREATED, result.mission().getStatus());
        assertEquals("info_a", result.mission().getFromNodeId());
        assertEquals("social_science_front", result.mission().getToNodeId());
        assertFalse(result.robotConnected());
        assertEquals(1, missionRepository.findAllByOrderIdOrderByCreatedAtAsc(order.getId()).size());
        assertEquals(OrderStatus.DELIVERING, orderRepository.findById(order.getId()).orElseThrow().getStatus());
        assertEquals(
                RobotStatus.DELIVERY_ASSIGNED,
                robotRepository.findById(Robot.SINGLE_ROBOT_KEY).orElseThrow().getStatus()
        );
    }

    @Test
    void completingDeliveryShouldCreateReturnMission() {
        var order = orderService.createMobileOrder(sampleCreateRequest());
        var deliveryMission = missionService.assignDeliveryMission(order.getId(), sampleAssignRequest()).mission();
        arrive(deliveryMission.getMissionId());

        var result = missionService.completeDelivery(
                order.getId(),
                new CompleteDeliveryRequest("Item received.")
        );

        assertEquals(MissionStatus.COMPLETED, result.deliveryMission().getStatus());
        assertEquals(MissionType.RETURN, result.returnMission().getType());
        assertEquals("social_science_front", result.returnMission().getFromNodeId());
        assertEquals(MissionService.BASE_NODE_ID, result.returnMission().getToNodeId());
        assertEquals(MissionStatus.CREATED, result.returnMission().getStatus());
        assertEquals(RobotStatus.RETURNING, robotRepository.findById(Robot.SINGLE_ROBOT_KEY).orElseThrow().getStatus());
        assertEquals(2, missionRepository.findAllByOrderIdOrderByCreatedAtAsc(order.getId()).size());
    }

    @Test
    void busyRobotShouldRejectNewDeliveryMission() {
        var firstOrder = orderService.createMobileOrder(sampleCreateRequest());
        var secondOrder = orderService.createMobileOrder(sampleCreateRequest());
        missionService.assignDeliveryMission(firstOrder.getId(), sampleAssignRequest());

        assertThrows(
                RobotUnavailableException.class,
                () -> missionService.assignDeliveryMission(secondOrder.getId(), sampleAssignRequest())
        );
    }

    @Test
    void robotMissionEventsShouldUpdateMissionStatus() {
        var order = orderService.createMobileOrder(sampleCreateRequest());
        var mission = missionService.assignDeliveryMission(order.getId(), sampleAssignRequest()).mission();

        var acked = missionService.handleRobotEvent(event(mission.getMissionId(), RobotMissionEventType.MISSION_ACK));
        var started = missionService.handleRobotEvent(event(mission.getMissionId(), RobotMissionEventType.MISSION_STARTED));
        var arrived = missionService.handleRobotEvent(event(mission.getMissionId(), RobotMissionEventType.ARRIVED_AT_DESTINATION));

        assertEquals(MissionStatus.ACKED, acked.getStatus());
        assertEquals(MissionStatus.IN_PROGRESS, started.getStatus());
        assertEquals(MissionStatus.ARRIVED, arrived.getStatus());
    }

    @Test
    void disconnectedRobotShouldStillPersistMission() {
        var order = orderService.createMobileOrder(sampleCreateRequest());

        var result = missionService.assignDeliveryMission(order.getId(), sampleAssignRequest());

        assertFalse(result.robotConnected());
        assertEquals("Mission saved, but robot is not connected.", result.dispatchMessage());
        assertEquals(1, missionRepository.findAll().size());
        assertEquals(MissionStatus.CREATED, missionRepository.findById(result.mission().getMissionId()).orElseThrow().getStatus());
    }

    @Test
    void duplicateDeliveryCompletionShouldNotCreateDuplicateReturnMission() {
        var order = orderService.createMobileOrder(sampleCreateRequest());
        var deliveryMission = missionService.assignDeliveryMission(order.getId(), sampleAssignRequest()).mission();
        arrive(deliveryMission.getMissionId());

        missionService.completeDelivery(order.getId(), new CompleteDeliveryRequest("Item received."));
        var duplicateResult = missionService.completeDelivery(order.getId(), new CompleteDeliveryRequest("Item received again."));

        long returnMissionCount = missionRepository.findAllByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .filter(mission -> mission.getType() == MissionType.RETURN)
                .count();

        assertFalse(duplicateResult.returnMissionCreated());
        assertEquals(1, returnMissionCount);
    }

    private void arrive(java.util.UUID missionId) {
        missionService.handleRobotEvent(event(missionId, RobotMissionEventType.MISSION_ACK));
        missionService.handleRobotEvent(event(missionId, RobotMissionEventType.MISSION_STARTED));
        missionService.handleRobotEvent(event(missionId, RobotMissionEventType.ARRIVED_AT_DESTINATION));
    }

    private RobotMissionEventRequest event(java.util.UUID missionId, RobotMissionEventType eventType) {
        return new RobotMissionEventRequest(
                missionId,
                eventType,
                1,
                BigDecimal.valueOf(37.375226),
                BigDecimal.valueOf(126.633868),
                eventType.name()
        );
    }

    private AssignDeliveryMissionRequest sampleAssignRequest() {
        return new AssignDeliveryMissionRequest(
                MissionService.BASE_NODE_ID,
                "social_science_front"
        );
    }

    private CreateOrderRequest sampleCreateRequest() {
        return new CreateOrderRequest(
                "Alice",
                "010-0000-0000",
                "social_science_front",
                List.of(new CreateOrderItemRequest("Sandwich", 2, BigDecimal.valueOf(5500)))
        );
    }
}
