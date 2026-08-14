package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.config.AdminStatusTracker;
import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.ChatMessageResponse;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.ChatMessageRepository;
import com.example.MobilePaluwagan.repository.ChatTicketRepository;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import com.example.MobilePaluwagan.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatTicketService {

    private final ChatTicketRepository chatTicketRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AdminStatusTracker adminStatusTracker;
    private final SseController sseController;
    private final CustomerServiceAIService customerServiceAIService;
    private final UserRepo userRepo;
    private final UserInfoRepo userInfoRepo;
    private final SimpMessagingTemplate messagingTemplate;




public Map<String, Object> getMessages(String ticketId) {
    Optional<ChatTicket> ticket = chatTicketRepository.findById(ticketId);

    if (ticket.isPresent()) {
        List<ChatMessage> messages = chatMessageRepository
                .findByTicketIdOrderByCreatedAtAsc(ticket.get().getId());
        List<ChatMessageResponse> mappedMessages = messages.stream()
                .map(msg -> {
                    UserInfo info = userInfoRepo.findByUserId(msg.getUserId())
                            .orElseThrow();

                    String name;
                    if (msg.getSentBy().equals("ADMIN")) {
                        name = "Admin " + info.getFirstName();
                    } else {
                        name = info.getFirstName() + " " + info.getLastName();
                    }

                    return ChatMessageResponse.builder()
                            .ticketId(msg.getTicketId())
                            .userId(msg.getUserId())
                            .message(msg.getMessage())
                            .sentBy(msg.getSentBy())
                            .createdAt(msg.getCreatedAt())
                            .senderName(name)
                            .senderProfileImage(info.getProfileImage())
                            .build();
                })
                .toList();

        return Map.of(
                "messages", mappedMessages,
                "ticketId", ticket.get().getId()
        );
    }

    return Map.of("messages", List.of());
}


    public Map<String, Object> getMessagesUser(Long userId) {
        Optional<ChatTicket> ticket = chatTicketRepository.findByUserIdAndStatusIn(
                userId, List.of(TicketStatus.PENDING, TicketStatus.OPEN, TicketStatus.AI_RESPONSE)
        );

        if (ticket.isPresent()) {
            List<ChatMessage> messages = chatMessageRepository
                    .findByTicketIdOrderByCreatedAtAsc(ticket.get().getId());
            List<ChatMessageResponse> mappedMessages = messages.stream()
                    .map(msg -> {
                        UserInfo info = userInfoRepo.findByUserId(msg.getUserId())
                                .orElseThrow();

                        String name;
                        if (msg.getSentBy().equals("ADMIN")) {
                            name = "Admin " + info.getFirstName();
                        } else {
                            name = info.getFirstName() + " " + info.getLastName();
                        }

                        return ChatMessageResponse.builder()
                                .ticketId(msg.getTicketId())
                                .userId(msg.getUserId())
                                .message(msg.getMessage())
                                .sentBy(msg.getSentBy())
                                .createdAt(msg.getCreatedAt())
                                .senderName(name)
                                .senderProfileImage(info.getProfileImage())
                                .build();
                    })
                    .toList();

            return Map.of(
                    "messages", mappedMessages,
                    "ticketId", ticket.get().getId()
            );
        }

        return Map.of("messages", List.of());
    }

    public ChatMessage sendMessage(String ticketId, Long userId, String message, String sentBy) {
        ChatTicket ticket = chatTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found."));

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new IllegalArgumentException("Ticket has already been closed.");
        }

        ChatMessage chatMessage = saveMessage(ticketId, userId, message, sentBy);

        UserInfo senderInfo = userInfoRepo.findByUserId(userId).orElseThrow();
        String senderName = sentBy.equals("ADMIN")
                ? "Admin " + senderInfo.getFirstName()
                : senderInfo.getFirstName() + " " + senderInfo.getLastName();

        ChatMessageResponse payload = ChatMessageResponse.builder()
                .ticketId(ticketId)
                .userId(userId)
                .message(message)
                .sentBy(sentBy)
                .createdAt(chatMessage.getCreatedAt())
                .senderName(senderName)
                .senderProfileImage(senderInfo.getProfileImage())
                .build();
        // para sa frontend sub to kung sino naka sub dto sila lang may convo
        messagingTemplate.convertAndSend("/topic/chat/" + ticketId, payload);

        if (sentBy.equals("USER")) {
            if (ticket.getStatus() == TicketStatus.AI_RESPONSE) {
                customerServiceAIService.processUserReplyForAITicket(ticket, message);
            }
        }

//        original logic
//        if(sentBy.equals("ADMIN")) {
//            ChatTicket ticket = chatTicketRepository.findById(ticketId).orElseThrow();
//            Long userIdOfTicket = ticket.getUserId();
//            sseController.notifyUserNewChatMessage(userIdOfTicket, message, sentBy, ticketId);
//        }else{
//            sseController.notifyAdminNewChatMessage(userId, message, ticketId);
//        }

        return chatMessage;
    }

    @Transactional
    public Map<String, Object> closeTicket(String ticketId) {
        ChatTicket ticket = chatTicketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        chatTicketRepository.save(ticket);

        chatMessageRepository.deleteByTicketId(ticketId);

        // notify user the ticket is closed
        messagingTemplate.convertAndSend("/topic/chat/" + ticketId,
                Map.of(
                        "type", "TICKET_CLOSED",
                        "message", "Ticket is solved. Admin close the ticket thank you!",
                        "ticketId", ticketId
                ));

        sseController.notifyUserTicketClosed(ticket.getUserId(), ticketId);

        Optional<ChatTicket> nextTicket = chatTicketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.PENDING);

        if(nextTicket.isPresent()) {
            ChatTicket next = nextTicket.get();
            next.setStatus(TicketStatus.OPEN);
            next.setOpenedAt(LocalDateTime.now());
            chatTicketRepository.save(next);

            saveMessage(next.getId(), next.getUserId(), next.getInitialMessage(), "USER");

            // notify the admin for the next ticket
            messagingTemplate.convertAndSend("/topic/chat/" + next.getId(),
                    Map.of(
                            "type", "TICKET_OPEN",
                            "message", "kinokonect ka na sa admin!",
                            "ticketId", next.getId()
                    ));

//            sseController.notifyUserTicketOpen(ticket.getUserId(), ticketId);

            messagingTemplate.convertAndSend("/topic/chat/" + next.getId(),
                    Map.of(
                            "type", "NEW_MESSAGE",
                            "userId", next.getUserId(),
                            "ticketId", next.getId(),
                            "message", next.getInitialMessage(),
                            "sentBy", "USER"
                    ));

            return Map.of(
                    "message", "Ticket closed! Next membro is ready.",
                    "nextTicketId", next.getId(),
                    "nextUserId", next.getUserId()
            );
        }

        return Map.of(
                "message", "Ticket closed! Wala ng pending na membero."
        );
    }

    public ApiResponse<Map<String, Object>> claimTicket(String ticketId, long adminId) {
        Optional<ChatTicket> existingTicket = chatTicketRepository.findById(ticketId);

        boolean hasActiveClaim = chatTicketRepository.existsByClaimedByAndStatusIn(
                adminId, List.of(TicketStatus.OPEN, TicketStatus.PENDING)
        );

        if (hasActiveClaim) {
            return new ApiResponse<>(false, "Ticket not found", null);
        }

        ChatTicket ticket = existingTicket.get();

        // Guard: baka na-claim na ng ibang admin
        if (!ticket.getStatus().equals(TicketStatus.PENDING)) {
            return new ApiResponse<>(false, "Ticket is no longer available", null);
        }

        ticket.setClaimedBy(adminId);
        ticket.setClaimedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.OPEN);
        chatTicketRepository.save(ticket);

        return new ApiResponse<>(true, "Ticket claimed successfully", Map.of(
                "ticketId", ticket.getId(),
                "status", ticket.getStatus()
        ));
    }

    private ChatMessage saveMessage(String ticketId, Long userId, String message, String sentBy) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTicketId(ticketId);
        chatMessage.setUserId(userId);
        chatMessage.setMessage(message);
        chatMessage.setSentBy(sentBy);
        chatMessage.setCreatedAt(LocalDateTime.now());
        return chatMessageRepository.save(chatMessage);
    }



}
