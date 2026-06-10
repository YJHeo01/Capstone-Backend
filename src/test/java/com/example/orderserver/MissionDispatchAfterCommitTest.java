package com.example.orderserver;

import com.example.orderserver.domain.MissionStatus;
import com.example.orderserver.dto.AssignDeliveryMissionRequest;
import com.example.orderserver.dto.CreateOrderItemRequest;
import com.example.orderserver.dto.CreateOrderRequest;
import com.example.orderserver.repository.MissionRepository;
import com.example.orderserver.repository.OrderRepository;
import com.example.orderserver.repository.RobotRepository;
import com.example.orderserver.service.MissionService;
import com.example.orderserver.service.OrderService;
import com.example.orderserver.service.RobotCommandGateway;
import com.example.orderserver.service.RobotCommandSendResult;
import com.example.orderserver.domain.Mission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {
        OrderServerApplication.class,
        MissionDispatchAfterCommitTest.FakeGatewayConfig.class
})
class MissionDispatchAfterCommitTest {

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

    @Autowired
    private TestRobotCommandGateway robotCommandGateway;

    @BeforeEach
    void setUp() {
        missionRepository.deleteAll();
        robotRepository.deleteAll();
        orderRepository.deleteAll();
        robotCommandGateway.reset();
    }

    @Test
    void routeShouldBeSentOnlyAfterMissionCommit() {
        var order = orderService.createMobileOrder(sampleCreateRequest());

        var result = missionService.assignDeliveryMission(
                order.getId(),
                new AssignDeliveryMissionRequest(MissionService.BASE_NODE_ID, "social_science_front")
        );

        assertEquals(result.mission().getMissionId(), robotCommandGateway.sentMissionId());
        assertTrue(robotCommandGateway.missionVisibleInNewTransactionAtSend());
        assertEquals(
                MissionStatus.DISPATCHED,
                missionRepository.findById(result.mission().getMissionId()).orElseThrow().getStatus()
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

    @TestConfiguration
    static class FakeGatewayConfig {

        @Bean
        @Primary
        TestRobotCommandGateway testRobotCommandGateway(
                MissionRepository missionRepository,
                PlatformTransactionManager transactionManager
        ) {
            return new TestRobotCommandGateway(missionRepository, transactionManager);
        }
    }

    static class TestRobotCommandGateway implements RobotCommandGateway {

        private final MissionRepository missionRepository;
        private final TransactionTemplate transactionTemplate;
        private UUID sentMissionId;
        private boolean missionVisibleInNewTransactionAtSend;

        TestRobotCommandGateway(
                MissionRepository missionRepository,
                PlatformTransactionManager transactionManager
        ) {
            this.missionRepository = missionRepository;
            this.transactionTemplate = new TransactionTemplate(transactionManager);
            this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        }

        @Override
        public RobotCommandSendResult sendMission(Mission mission) {
            this.sentMissionId = mission.getMissionId();
            Boolean missionExists = transactionTemplate.execute(status ->
                    missionRepository.existsById(mission.getMissionId())
            );
            this.missionVisibleInNewTransactionAtSend = Boolean.TRUE.equals(missionExists);
            return new RobotCommandSendResult(true, "Mission route sent to robot.");
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        UUID sentMissionId() {
            assertNotNull(sentMissionId);
            return sentMissionId;
        }

        boolean missionVisibleInNewTransactionAtSend() {
            return missionVisibleInNewTransactionAtSend;
        }

        void reset() {
            sentMissionId = null;
            missionVisibleInNewTransactionAtSend = false;
        }
    }
}
