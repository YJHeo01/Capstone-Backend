package com.example.orderserver.exception;

import java.util.UUID;

public class MissionNotFoundException extends RuntimeException {

    public MissionNotFoundException(UUID missionId) {
        super("Mission not found: " + missionId);
    }
}
