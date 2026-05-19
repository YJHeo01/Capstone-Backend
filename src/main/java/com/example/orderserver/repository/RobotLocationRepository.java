package com.example.orderserver.repository;

import com.example.orderserver.domain.RobotLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotLocationRepository extends JpaRepository<RobotLocation, Long> {
}
