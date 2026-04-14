package com.example.orderserver.service;

import com.example.orderserver.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderRealtimeService {

    private static final long STREAM_TIMEOUT_MS = 30L * 60L * 1000L;

    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID orderId, OrderResponse initialSnapshot) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        CopyOnWriteArrayList<SseEmitter> orderEmitters =
                emitters.computeIfAbsent(orderId, ignored -> new CopyOnWriteArrayList<>());

        orderEmitters.add(emitter);
        emitter.onCompletion(() -> removeEmitter(orderId, emitter));
        emitter.onTimeout(() -> removeEmitter(orderId, emitter));
        emitter.onError(error -> removeEmitter(orderId, emitter));

        sendEvent(orderId, emitter, "order-snapshot", initialSnapshot);
        return emitter;
    }

    public void publishOrderUpdate(OrderResponse response) {
        broadcast(response.id(), "order-updated", response);
    }

    public void publishOrderDeleted(UUID orderId) {
        broadcast(orderId, "order-deleted", Map.of(
                "orderId", orderId,
                "deleted", true
        ));
        completeAll(orderId);
    }

    private void broadcast(UUID orderId, String eventName, Object payload) {
        CopyOnWriteArrayList<SseEmitter> orderEmitters = emitters.get(orderId);
        if (orderEmitters == null || orderEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : orderEmitters) {
            sendEvent(orderId, emitter, eventName, payload);
        }
    }

    private void sendEvent(UUID orderId, SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(payload));
        } catch (IOException exception) {
            emitter.completeWithError(exception);
            removeEmitter(orderId, emitter);
        }
    }

    private void completeAll(UUID orderId) {
        CopyOnWriteArrayList<SseEmitter> orderEmitters = emitters.remove(orderId);
        if (orderEmitters == null) {
            return;
        }

        for (SseEmitter emitter : orderEmitters) {
            emitter.complete();
        }
    }

    private void removeEmitter(UUID orderId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> orderEmitters = emitters.get(orderId);
        if (orderEmitters == null) {
            return;
        }

        orderEmitters.remove(emitter);
        if (orderEmitters.isEmpty()) {
            emitters.remove(orderId, orderEmitters);
        }
    }
}
