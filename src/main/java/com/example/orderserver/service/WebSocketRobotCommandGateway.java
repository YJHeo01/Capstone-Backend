package com.example.orderserver.service;

import com.example.orderserver.controller.MissionMapper;
import com.example.orderserver.domain.Mission;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebSocketRobotCommandGateway implements RobotCommandGateway {

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> robotSessions = new CopyOnWriteArraySet<>();

    public WebSocketRobotCommandGateway(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(WebSocketSession session) {
        robotSessions.add(session);
    }

    public void unregister(WebSocketSession session) {
        robotSessions.removeIf(currentSession -> currentSession.getId().equals(session.getId()));
    }

    @Override
    public RobotCommandSendResult sendMission(Mission mission) {
        robotSessions.removeIf(session -> !session.isOpen());
        if (robotSessions.isEmpty()) {
            return new RobotCommandSendResult(false, "Mission saved, but robot is not connected.");
        }

        try {
            String payload = objectMapper.writeValueAsString(MissionMapper.toCommandMessage(mission));
            int sentCount = sendToOpenSessions(payload);
            if (sentCount == 0) {
                return new RobotCommandSendResult(false, "Mission saved, but sending route to robot failed.");
            }
            return new RobotCommandSendResult(true, "Mission route sent to robot.");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize mission route.", exception);
        }
    }

    @Override
    public boolean isConnected() {
        robotSessions.removeIf(session -> !session.isOpen());
        return !robotSessions.isEmpty();
    }

    private int sendToOpenSessions(String payload) {
        int sentCount = 0;
        for (WebSocketSession session : robotSessions) {
            try {
                session.sendMessage(new TextMessage(payload));
                sentCount++;
            } catch (IOException exception) {
                robotSessions.remove(session);
            }
        }
        return sentCount;
    }
}
