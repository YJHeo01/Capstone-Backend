package com.example.orderserver.repository;

import com.example.orderserver.domain.RobotRouteEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RobotRouteEventRepository extends JpaRepository<RobotRouteEvent, Long> {

    List<RobotRouteEvent> findAllByOrderByReceivedAtDesc();

    List<RobotRouteEvent> findAllByCommandIdOrderByReceivedAtAsc(String commandId);
}
