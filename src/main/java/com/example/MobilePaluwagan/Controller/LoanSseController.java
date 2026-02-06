package com.example.MobilePaluwagan.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class LoanSseController {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/loan/updates")
    public SseEmitter loanUpdates(String nagUpdateAngAdmin) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        System.out.println("New SSE client connected. Total clients: " + emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            System.out.println("Client disconnected. Remaining clients: " + emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            System.out.println("Client timed out. Remaining clients: " + emitters.size());
        });

        return emitter;
    }

    public void notifyLoan() {
        System.out.println("Broadcasting update: " + " to " + emitters.size() + " clients");
        for (SseEmitter emitter : emitters){
            try {
                emitter.send(SseEmitter.event()
                        .name("loan-update")
                        .data("SSE is working!"));
                System.out.println("Sent to client successfully");
            }catch (IOException e ){
                emitters.remove(emitter);
                System.out.println("Failed to send, removed client");
            }
        }
    }
}
