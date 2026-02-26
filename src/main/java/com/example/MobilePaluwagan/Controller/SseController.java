package com.example.MobilePaluwagan.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api")
public class SseController {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();


    @GetMapping("/loan/updates")
    public SseEmitter loanUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
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
                System.out.println("Sent to client successfully");
            }catch (IOException e ){
                deadEmitters.add(emitter);
                System.out.println("Failed to send, removed client");
            }
        }

        emitters.removeAll(deadEmitters);
    }
}
