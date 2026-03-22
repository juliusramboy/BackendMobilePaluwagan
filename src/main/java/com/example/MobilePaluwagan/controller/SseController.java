package com.example.MobilePaluwagan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api")
public class SseController {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    @GetMapping("/loan/updates")
    public SseEmitter loanUpdates() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError((e) -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connect").data("Connected!"));
        } catch (IOException e) {
            emitters.remove(emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void notifyUpdate() {

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters){
            try {
                emitter.send(SseEmitter.event()
                        .name("loan-update")
                        .data("SSE is working!"));
                //System.out.println("Sent to client successfully");
            }catch (IOException e ){
                deadEmitters.add(emitter);
                System.out.println("Failed to send, removed client");
            }
        }

        emitters.removeAll(deadEmitters);
    }

    // Notify admin of new chat message
    public void notifyAdminNewChatMessage(Long userId, String message, String ticketId) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
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
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    //  Notify specific user of admin reply
    public void notifyUserNewChatMessage(Long userId, String message, String sentBy) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("chat-message-" + userId)
                        .data(Map.of(
                                "message", message,
                                "sentBy", sentBy
                        )));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    //  Notify specific user ticket is open
    public void notifyUserTicketOpen(Long userId) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ticket-open-" + userId)
                        .data(Map.of(
                                "message", "Iko-connect ka na sa admin!"
                        )));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    //  Notify specific user ticket is closed
    public void notifyUserTicketClosed(Long userId) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ticket-closed-" + userId)
                        .data(Map.of(
                                "message", "Ang chat ay natapos na. Salamat!"
                        )));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

}
