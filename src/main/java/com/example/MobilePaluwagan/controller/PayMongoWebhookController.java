package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.annotation.Idempotent;
import com.example.MobilePaluwagan.service.PayMongoService;
import com.example.MobilePaluwagan.service.PayMongoWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class PayMongoWebhookController {

    private final PayMongoWebhookService payMongoWebhookService;

    private final WebClient webClient;

    @Value("${paymongo.secret-key}")
    private String secretKey;

    @PostMapping("/paymongo")
    public ResponseEntity<?> handleWebhook(@RequestBody String payload, @RequestHeader("Paymongo-Signature") String signature){
        try {
            payMongoWebhookService.processWebhook(payload, signature);
            return ResponseEntity.ok(Map.of("message", "Webhook received"));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @PostMapping("/register")
//    public ResponseEntity<?> registerWebhook() {
//        try {
//            String credentials = Base64.getEncoder()
//                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
//
//            Map<String, Object> attributes = new HashMap<>();
//            attributes.put("url", "https://practitioners-rise-herself-baseline.trycloudflare.com/api/webhook/paymongo");
//            attributes.put("events", List.of("link.payment.paid"));
//
//            Map<String, Object> data = new HashMap<>();
//            data.put("attributes", attributes);
//
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("data", data);
//
//            Map<String, Object> response = webClient.post()
//                    .uri("/webhooks")
//                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    @GetMapping("/list")
//    public ResponseEntity<?> listWebhooks() {
//        try {
//            String credentials = Base64.getEncoder()
//                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
//
//            Map<String, Object> response = webClient.get()
//                    .uri("/webhooks")
//                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
//        }
//    }
//
//    @PutMapping("/update/{webhookId}")
//    public ResponseEntity<?> updateWebhook(@PathVariable String webhookId) {
//        try {
//            String credentials = Base64.getEncoder()
//                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
//
//            Map<String, Object> attributes = new HashMap<>();
//            attributes.put("url", "https://examining-notified-explained-relevant.trycloudflare.com/api/webhook/paymongo");
//            attributes.put("events", List.of("link.payment.paid"));
//
//            Map<String, Object> data = new HashMap<>();
//            data.put("attributes", attributes);
//
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("data", data);
//
//            Map<String, Object> response = webClient.put()
//                    .uri("/webhooks/" + webhookId)
//                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
//        }
//    }
}
