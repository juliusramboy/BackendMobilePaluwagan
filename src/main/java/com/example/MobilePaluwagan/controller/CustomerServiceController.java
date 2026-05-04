package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.config.AdminStatusTracker;
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
    private final AdminStatusTracker adminStatusTracker;


    //sa una pag mag rrequest yung user
    @PostMapping("/user/request")
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> requestChat(@RequestBody ChatRequest request, Authentication authentication) {
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        Long userId = userPrinciple.userId();
        return customerServiceAIService.filterRawData(request.getMessage(),userId);
    }

    // pag mag uusap na sila ng admin (para ma send yung chat nya sa admin)
    @PostMapping("/user/ticket/{ticketId}/message")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> sendMessage(
            @PathVariable String ticketId,
            @RequestBody ChatRequest request,
            Authentication authentication) {
        try {UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
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

    //get all the msgs within the ticket id yan
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

    // i cclose na ni admin yung convo nila by ticket
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

    // mag rreply yung admin sa user
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

    // para sa pending ticket kung meron pang get
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
