package com.example.orderserver.repository;

import com.example.orderserver.domain.RobotDispatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RobotDispatchRepository extends JpaRepository<RobotDispatch, UUID> {

    List<RobotDispatch> findAllByOrderByDispatchedAtDesc();
}
