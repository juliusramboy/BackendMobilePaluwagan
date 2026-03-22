package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.config.AdminStatusTracker;
import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.entity.ChatMessage;
import com.example.MobilePaluwagan.entity.ChatTicket;
import com.example.MobilePaluwagan.entity.TicketStatus;
import com.example.MobilePaluwagan.repository.ChatMessageRepository;
import com.example.MobilePaluwagan.repository.ChatTicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    public Map<String, Object> requestChat(String message, Long userId) {

        Optional<ChatTicket> existingTicket = chatTicketRepository.findByUserIdAndStatusIn(userId, List.of(TicketStatus.PENDING, TicketStatus.OPEN));

        if(existingTicket.isPresent()) {
            chatMessageRepository.deleteByTicketId(existingTicket.get().getId());
            chatTicketRepository.delete(existingTicket.get());
            System.out.println("Old ticket deleted for userId: " + userId);
        }

        if (!adminStatusTracker.isAnyAdminOnline()){
            String aiResponse = customerServiceAIService.chat(message);
            return Map.of(
                    "response:", aiResponse,
                    "answeredBy", "Peps",
                    "redirectToAdmin", false
            );
        }

        Optional<ChatTicket> openTicket = chatTicketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.OPEN);

        ChatTicket ticket = new ChatTicket();
        ticket.setUserId(userId);
        ticket.setInitialMessage(message);
        ticket.setCreatedAt(LocalDateTime.now());

        if (openTicket.isEmpty()) {
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setOpenedAt(LocalDateTime.now());
            chatTicketRepository.save(ticket);

            saveMessage(ticket.getId(), userId, message, "USER");

            sseController.notifyAdminNewChatMessage(userId, message, ticket.getId());

            return Map.of(
                    "message", "Maghintay ng ilang minuto kinokonect ka na sa admin.",
                    "ticketId", ticket.getId(),
                    "status", "OPEN",
                    "redirectToAdmin", true
            );
        }else {
            ticket.setStatus(TicketStatus.PENDING);
            chatTicketRepository.save(ticket);

            List<ChatTicket> pendingTicket = chatTicketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.PENDING);
            int position = pendingTicket.size();

            return Map.of(
                    "message", "May Kausap pa ang admin na Myembro nawa'y maghintay ng ilang minuto. " +
                            "Ikaw ay #" + position + " sa pila. " +
                            "Pakihintay.",
                    "ticketId", ticket.getId(),
                    "status", "PENDING",
                    "queuePosition", position
            );
        }
    }

    public List<ChatMessage> getMessages(String ticketId) {
        return chatMessageRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId);
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
