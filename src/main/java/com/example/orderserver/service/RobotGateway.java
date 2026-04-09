package com.example.orderserver.service;

import com.example.orderserver.domain.Order;
import com.example.orderserver.domain.RobotDispatch;

import java.util.List;

public interface RobotGateway {

    RobotDispatch sendOrder(Order order);

    List<RobotDispatch> getDispatchHistory();
}
