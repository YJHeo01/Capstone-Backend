package com.example.orderserver.controller;

import com.example.orderserver.dto.RobotMissionEventRequest;
import com.example.orderserver.dto.RobotMissionEventType;
import com.example.orderserver.service.MissionService;
import com.example.orderserver.service.WebSocketRobotCommandGateway;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class RobotMissionWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketRobotCommandGateway robotCommandGateway;
    private final MissionService missionService;
    private final ObjectMapper objectMapper;

    public RobotMissionWebSocketHandler(
            WebSocketRobotCommandGateway robotCommandGateway,
            MissionService missionService,
            ObjectMapper objectMapper
    ) {
        this.robotCommandGateway = robotCommandGateway;
        this.missionService = missionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        robotCommandGateway.register(session);
        missionService.dispatchPendingMissions();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            RobotMissionEventRequest request = toEventRequest(message.getPayload());
            var mission = missionService.handleRobotEvent(request);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "messageType", "MISSION_EVENT_ACCEPTED",
                    "missionId", mission.getMissionId(),
                    "status", mission.getStatus()
            ))));
        } catch (RuntimeException exception) {
            sendError(session, exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        robotCommandGateway.unregister(session);
    }

    private RobotMissionEventRequest toEventRequest(String payload) throws IOException {
        JsonNode root = objectMapper.readTree(payload);
        UUID missionId = UUID.fromString(requiredText(root, "missionId"));

        String eventTypeText = optionalText(root, "eventType");
        if (!StringUtils.hasText(eventTypeText)) {
            eventTypeText = requiredText(root, "messageType");
        }

        return new RobotMissionEventRequest(
                missionId,
                RobotMissionEventType.valueOf(eventTypeText),
                optionalInteger(root, "currentWaypointSequence"),
                optionalBigDecimal(root, "latitude"),
                optionalBigDecimal(root, "longitude"),
                optionalText(root, "message")
        );
    }

    private String requiredText(JsonNode root, String fieldName) {
        String value = optionalText(root, fieldName);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }

    private String optionalText(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private Integer optionalInteger(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asInt();
    }

    private BigDecimal optionalBigDecimal(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "messageType", "ERROR",
                "message", message == null ? "Unknown websocket message error." : message
        ))));
    }
}
