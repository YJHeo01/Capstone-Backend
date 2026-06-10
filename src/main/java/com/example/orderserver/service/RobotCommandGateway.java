package com.example.orderserver.service;

import com.example.orderserver.domain.Mission;

public interface RobotCommandGateway {

    RobotCommandSendResult sendMission(Mission mission);

    boolean isConnected();
}
