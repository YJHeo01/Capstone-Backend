package com.example.orderserver.service;

public record RobotCommandSendResult(
        boolean robotConnected,
        String message
) {
}
