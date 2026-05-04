package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.config.AdminStatusTracker;
import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.dto.Response.ChatMessageResponse;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.ChatMessageRepository;
import com.example.MobilePaluwagan.repository.ChatTicketRepository;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import com.example.MobilePaluwagan.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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



public List<ChatMessageResponse> getMessages(Long userId) {
    Optional<ChatTicket> ticket = chatTicketRepository.findByUserId(userId);

    if (ticket.isPresent()){
        List<ChatMessage> messages = chatMessageRepository
                .findByTicketIdOrderByCreatedAtAsc(ticket.get().getId());

        return messages.stream()
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
    }
    return null;
}

    public ChatMessage sendMessage(String ticketId, Long userId, String message, String sentBy) {
        ChatMessage chatMessage = saveMessage(ticketId, userId, message, sentBy);

        if(sentBy.equals("ADMIN")) {
            sseController.notifyUserNewChatMessage(userId, message, ticketId);
        }else{
            sseController.notifyAdminNewChatMessage(userId, message, ticketId);
        }

        return chatMessage;
    }

    @Transactional
    public Map<String, Object> closeTicket(String ticketId) {
        ChatTicket ticket = chatTicketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        chatTicketRepository.save(ticket);

        chatMessageRepository.deleteByTicketId(ticketId);

        sseController.notifyUserTicketClosed(ticket.getUserId());

        Optional<ChatTicket> nextTicket = chatTicketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.PENDING);

        if(nextTicket.isPresent()) {
            ChatTicket next = nextTicket.get();
            next.setStatus(TicketStatus.OPEN);
            next.setOpenedAt(LocalDateTime.now());
            chatTicketRepository.save(next);

            saveMessage(next.getId(), next.getUserId(), next.getInitialMessage(), "USER");

            sseController.notifyUserTicketOpen(next.getUserId());

            sseController.notifyAdminNewChatMessage(next.getUserId(), next.getInitialMessage(), next.getId());

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
