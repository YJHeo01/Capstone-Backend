package com.example.orderserver.config;

import com.example.orderserver.controller.RobotMissionWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RobotMissionWebSocketHandler robotMissionWebSocketHandler;

    public WebSocketConfig(RobotMissionWebSocketHandler robotMissionWebSocketHandler) {
        this.robotMissionWebSocketHandler = robotMissionWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(robotMissionWebSocketHandler, "/ws/robot")
                .setAllowedOrigins("*");
    }
}
