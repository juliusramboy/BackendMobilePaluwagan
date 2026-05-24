package com.example.MobilePaluwagan.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api")
public class SseController {

    @Autowired
    private TaskScheduler taskScheduler;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> ticketEmitters = new ConcurrentHashMap<>();

    // BAGO: Single global heartbeat para sa ticket emitters lang
    @PostConstruct
    public void startHeartbeat() {
        taskScheduler.scheduleAtFixedRate(() -> {

            // Ping lahat ng ticket emitters (/api/cs/ticket/{ticketId}/subscribe)
            for (Map.Entry<String, CopyOnWriteArrayList<SseEmitter>> entry : ticketEmitters.entrySet()) {
                List<SseEmitter> deadTicket = new ArrayList<>();
                for (SseEmitter emitter : entry.getValue()) {
                    try {
                        emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
                    } catch (IOException e) {
                        deadTicket.add(emitter);
                    }
                }
                entry.getValue().removeAll(deadTicket);

                if (entry.getValue().isEmpty()) {
                    ticketEmitters.remove(entry.getKey());
                }
            }

        }, Duration.ofSeconds(25));
    }


    @GetMapping("/loan/updates")
    public SseEmitter loanUpdates() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> { emitters.remove(emitter); emitter.complete(); });
        emitter.onError((e) -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected!"));
        } catch (IOException e) {
            emitters.remove(emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    // BINAGO: 30 mins timeout → 0L (walang timeout)
    @GetMapping("/cs/ticket/{ticketId}/subscribe")
    public SseEmitter subscribeToTicket(@PathVariable String ticketId) {
        SseEmitter emitter = new SseEmitter(0L);
        ticketEmitters.computeIfAbsent(ticketId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        System.out.println("=== New SSE Subscription ===");
        System.out.println("TicketId: " + ticketId);
        System.out.println("Total emitters for ticket: " + ticketEmitters.get(ticketId).size());

        emitter.onCompletion(() -> removeEmitter(ticketId, emitter));
        emitter.onTimeout(() -> { removeEmitter(ticketId, emitter); emitter.complete(); });
        emitter.onError(e -> removeEmitter(ticketId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected to ticket: " + ticketId));
        } catch (IOException e) {
            removeEmitter(ticketId, emitter);
        }
        return emitter;
    }

    private void removeEmitter(String ticketId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = ticketEmitters.get(ticketId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) ticketEmitters.remove(ticketId);
            System.out.println("Emitter removed for ticket: " + ticketId);
        }
    }


    public void notifyUpdate() {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("loan-update")
                        .data("SSE is working!"));
            } catch (IOException e) {
                deadEmitters.add(emitter);
                System.out.println("Failed to send, removed client");
            }
        }
        emitters.removeAll(deadEmitters);
    }


    public void notifyAdminNewChatMessage(Long userId, String message, String ticketId) {
        List<SseEmitter> emitters = ticketEmitters.getOrDefault(ticketId, new CopyOnWriteArrayList<>());
        List<SseEmitter> dead = new ArrayList<>();

        System.out.println("=== notifyAdminNewChatMessage ===");
        System.out.println("TicketId: " + ticketId);
        System.out.println("UserId: " + userId);
        System.out.println("Message: " + message);
        System.out.println("Emitters count: " + emitters.size());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("chat-notification")
                        .data(Map.of(
                                "userId", userId,
                                "ticketId", ticketId,
                                "message", message,
                                "sentBy", "USER"
                        )));
                System.out.println("✓ Sent to emitter successfully");
            } catch (IOException e) {
                dead.add(emitter);
                System.out.println("✗ Failed to send, removing emitter: " + e.getMessage());
            }
        }
        emitters.removeAll(dead);
        System.out.println("Dead emitters removed: " + dead.size());
    }


    public void notifyUserNewChatMessage(Long userId, String message, String sentBy, String ticketId) {
        List<SseEmitter> emitters = ticketEmitters.getOrDefault(ticketId, new CopyOnWriteArrayList<>());
        List<SseEmitter> dead = new ArrayList<>();

        System.out.println("=== notifyUserNewChatMessage ===");
        System.out.println("TicketId: " + ticketId);
        System.out.println("UserId: " + userId);
        System.out.println("Message: " + message);
        System.out.println("SentBy: " + sentBy);
        System.out.println("Emitters count: " + emitters.size());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("chat-message-" + userId)
                        .data(Map.of(
                                "userId", userId,
                                "ticketId", ticketId,
                                "message", message,
                                "sentBy", sentBy
                        )));
                System.out.println("✓ Sent to emitter successfully");
            } catch (IOException e) {
                dead.add(emitter);
                System.out.println("✗ Failed to send, removing emitter: " + e.getMessage());
            }
        }
        emitters.removeAll(dead);
        System.out.println("Dead emitters removed: " + dead.size());
    }

    //  BINAGO: gumagamit na ng ticketEmitters + may ticketId param
    public void notifyUserTicketOpen(Long userId, String ticketId) {
        List<SseEmitter> emitters = ticketEmitters.getOrDefault(ticketId, new CopyOnWriteArrayList<>());
        List<SseEmitter> dead = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ticket-open-" + userId)
                        .data(Map.of(
                                "message", "Iko-connect ka na sa admin!",
                                "ticketId", ticketId
                        )));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }

    //  BINAGO: gumagamit na ng ticketEmitters + may ticketId param + cleanup
    public void notifyUserTicketClosed(Long userId, String ticketId) {
        List<SseEmitter> emitters = ticketEmitters.getOrDefault(ticketId, new CopyOnWriteArrayList<>());
        List<SseEmitter> dead = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ticket-closed-" + userId)
                        .data(Map.of(
                                "message", "Ang chat ay natapos na. Salamat!",
                                "ticketId", ticketId
                        )));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);

        ticketEmitters.remove(ticketId);
        System.out.println("Ticket closed and emitters cleaned up for: " + ticketId);
    }
}