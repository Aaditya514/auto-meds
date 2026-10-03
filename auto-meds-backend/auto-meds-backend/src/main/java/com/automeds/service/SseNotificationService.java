package com.automeds.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: SseNotificationService
 * Description: Real-time Server-Sent Events (SSE) notification and live telemetry push engine (P4).
 * Maintains thread-safe subscriber channels for active patients and clinical pharmacists,
 * pushing instant updates (OCR completion, prescription triage, order status transitions)
 * with zero browser polling.
 */
public class SseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SseNotificationService.class);

    // Default connection timeout: 1 hour (reconnect handled automatically by browser EventSource)
    private static final Long EMITTER_TIMEOUT = 60 * 60 * 1000L;

    // Patient subscribers mapped by patient userId -> list of active tabs/devices
    private final Map<Long, List<SseEmitter>> patientEmitters = new ConcurrentHashMap<>();

    // Clinical staff / pharmacist subscribers for pharmacy triage telemetry
    private final List<SseEmitter> staffEmitters = new CopyOnWriteArrayList<>();

    /**
     * Subscribe a patient to their private real-time clinical telemetry stream.
     */
    public SseEmitter subscribePatient(Long userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        patientEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removePatientEmitter(userId, emitter));
        emitter.onTimeout(() -> removePatientEmitter(userId, emitter));
        emitter.onError(e -> removePatientEmitter(userId, emitter));

        // Send initial connection acknowledgement event
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(Map.of("message", "Auto-Meds live telemetry stream established", "userId", userId)));
        } catch (IOException e) {
            removePatientEmitter(userId, emitter);
        }

        log.info("Patient #{} subscribed to real-time SSE telemetry. Active channels: {}", userId, patientEmitters.get(userId).size());
        return emitter;
    }

    /**
     * Subscribe a pharmacist or clinical administrator to the command center stream.
     */
    public SseEmitter subscribeStaff() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        staffEmitters.add(emitter);

        emitter.onCompletion(() -> staffEmitters.remove(emitter));
        emitter.onTimeout(() -> staffEmitters.remove(emitter));
        emitter.onError(e -> staffEmitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(Map.of("message", "Pharmacy command center stream established")));
        } catch (IOException e) {
            staffEmitters.remove(emitter);
        }

        log.info("Clinical staff member subscribed to SSE command center. Total active staff streams: {}", staffEmitters.size());
        return emitter;
    }

    /**
     * Send targeted live push event to a specific patient.
     */
    public void sendToPatient(Long userId, String eventType, Object data) {
        List<SseEmitter> emitters = patientEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }

    /**
     * Broadcast live clinical event to all active pharmacists / admins.
     */
    public void sendToStaff(String eventType, Object data) {
        if (staffEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : staffEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }

        staffEmitters.removeAll(deadEmitters);
    }

    /**
     * Broadcast global operational event to everyone (patients and staff).
     */
    public void broadcast(String eventType, Object data) {
        sendToStaff(eventType, data);
        patientEmitters.keySet().forEach(userId -> sendToPatient(userId, eventType, data));
    }

    /**
     * Periodic 25-second heartbeat ping to prevent proxy/NGINX idle connection closure.
     */
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        // Ping patient channels
        patientEmitters.forEach((userId, emitters) -> {
            List<SseEmitter> dead = new ArrayList<>();
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("PING").data("keepalive"));
                } catch (Exception ex) {
                    dead.add(emitter);
                }
            }
            emitters.removeAll(dead);
        });

        // Ping staff channels
        List<SseEmitter> deadStaff = new ArrayList<>();
        for (SseEmitter emitter : staffEmitters) {
            try {
                emitter.send(SseEmitter.event().name("PING").data("keepalive"));
            } catch (Exception ex) {
                deadStaff.add(emitter);
            }
        }
        staffEmitters.removeAll(deadStaff);
    }

    private void removePatientEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = patientEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                patientEmitters.remove(userId);
            }
        }
    }
}
