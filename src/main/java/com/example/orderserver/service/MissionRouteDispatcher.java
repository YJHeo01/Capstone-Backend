package com.example.orderserver.service;

import com.example.orderserver.domain.Mission;
import com.example.orderserver.domain.MissionStatus;
import com.example.orderserver.exception.MissionNotFoundException;
import com.example.orderserver.repository.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Service
public class MissionRouteDispatcher {

    private final MissionRepository missionRepository;
    private final RobotCommandGateway robotCommandGateway;
    private final TransactionTemplate transactionTemplate;

    public MissionRouteDispatcher(
            MissionRepository missionRepository,
            RobotCommandGateway robotCommandGateway,
            PlatformTransactionManager transactionManager
    ) {
        this.missionRepository = missionRepository;
        this.robotCommandGateway = robotCommandGateway;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public RobotCommandSendResult dispatch(UUID missionId) {
        if (!robotCommandGateway.isConnected()) {
            return new RobotCommandSendResult(false, "Mission saved, but robot is not connected.");
        }

        Mission mission = transactionTemplate.execute(status -> {
            Mission foundMission = missionRepository.findById(missionId)
                    .orElseThrow(() -> new MissionNotFoundException(missionId));
            if (foundMission.getStatus() == MissionStatus.CREATED) {
                foundMission.markDispatched();
            }
            return missionRepository.save(foundMission);
        });

        return robotCommandGateway.sendMission(mission);
    }
}
