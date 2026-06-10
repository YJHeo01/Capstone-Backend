package com.example.orderserver.repository;

import com.example.orderserver.domain.Robot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotRepository extends JpaRepository<Robot, String> {
}
