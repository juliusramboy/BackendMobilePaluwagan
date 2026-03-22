package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.dto.Request.ChatRequest;
import com.example.MobilePaluwagan.entity.ChatMessage;
import com.example.MobilePaluwagan.entity.ChatTicket;
import com.example.MobilePaluwagan.entity.TicketStatus;
import com.example.MobilePaluwagan.entity.UserPrinciple;
import com.example.MobilePaluwagan.repository.ChatTicketRepository;
import com.example.MobilePaluwagan.service.ChatTicketService;
import com.example.MobilePaluwagan.service.CustomerServiceAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class CustomerServiceController {

    private final CustomerServiceAIService customerServiceAIService;
    private final ChatTicketService chatTicketService;
    private final ChatTicketRepository  chatTicketRepository;

    @PostMapping
    public ResponseEntity<?> chat(
            @RequestBody ChatRequest request,
            Authentication authentication) {
        try {
            String response = customerServiceAIService
                    .chat(request.getMessage());
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> requestChat(@RequestBody ChatRequest request, Authentication authentication) {
        try{
            UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
            Long userId = userPrinciple.userId();
            Map<String, Object> response = chatTicketService.requestChat(request.getMessage(), userId);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/ticket/{ticketId}/message")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> sendMessage(
            @PathVariable String ticketId,
            @RequestBody ChatRequest request,
            Authentication authentication) {
        try {
            UserPrinciple userDetails =
                    (UserPrinciple) authentication.getPrincipal();
            Long userId = userDetails.userId();
            ChatMessage message = chatTicketService
                    .sendMessage(ticketId, userId,
                            request.getMessage(), "USER");
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ticket/{ticketId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable String ticketId) {
        try {
            return ResponseEntity.ok(
                    chatTicketService.getMessages(ticketId));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/admin/ticket/{ticketId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> closeTicket(
            @PathVariable String ticketId) {
        try {
            Map<String, Object> response = chatTicketService
                    .closeTicket(ticketId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }


    @PostMapping("/admin/ticket/{ticketId}/message")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminSendMessage(
            @PathVariable String ticketId,
            @RequestBody ChatRequest request,
            Authentication authentication) {
        try {
            UserPrinciple userDetails =
                    (UserPrinciple) authentication.getPrincipal();
            Long userId = userDetails.userId();
            ChatMessage message = chatTicketService
                    .sendMessage(ticketId, userId,
                            request.getMessage(), "ADMIN");
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingTickets() {
        try {
            List<ChatTicket> tickets = chatTicketRepository
                    .findByStatusOrderByCreatedAtAsc(
                            TicketStatus.PENDING);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
