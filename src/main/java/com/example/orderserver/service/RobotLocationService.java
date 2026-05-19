package com.example.orderserver.service;

import com.example.orderserver.domain.RobotLocation;
import com.example.orderserver.dto.RobotLocationRequest;
import com.example.orderserver.repository.RobotLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RobotLocationService {

    private final RobotLocationRepository robotLocationRepository;

    public RobotLocationService(RobotLocationRepository robotLocationRepository) {
        this.robotLocationRepository = robotLocationRepository;
    }

    @Transactional
    public RobotLocation saveLatestLocation(RobotLocationRequest request) {
        RobotLocation location = robotLocationRepository.findById(RobotLocation.SINGLE_ROBOT_ID)
                .orElseGet(() -> RobotLocation.createLatest(request.latitude(), request.longitude()));

        location.update(request.latitude(), request.longitude());
        return robotLocationRepository.save(location);
    }

    public Optional<RobotLocation> getLatestLocation() {
        return robotLocationRepository.findById(RobotLocation.SINGLE_ROBOT_ID);
    }
}
