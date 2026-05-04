package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.config.AdminStatusTracker;
import com.example.MobilePaluwagan.entity.ChatMessage;
import com.example.MobilePaluwagan.entity.ChatTicket;
import com.example.MobilePaluwagan.entity.TicketStatus;
import com.example.MobilePaluwagan.repository.ChatMessageRepository;
import com.example.MobilePaluwagan.repository.ChatTicketRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class CustomerServiceAIService {
    private final ChatClient chatClient;
    private final AdminStatusTracker adminStatus;
    private final ChatTicketRepository chatTicketRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Value("classpath:prompts/peep-system-prompt.st")
    private Resource peepSystemPrompt;

    @Value("classpath:prompts/peep-decision.st")
    private Resource peepSystemDecision;


    public CustomerServiceAIService(ChatClient chatClient, AdminStatusTracker adminStatus, ChatTicketRepository chatTicketRepository, ChatMessageRepository chatMessageRepository){
        this.chatClient = chatClient;
        this.adminStatus = adminStatus;
        this.chatTicketRepository = chatTicketRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    private String chatDesc(String userPrompt){
        return chatClient
                .prompt()
                .system(peepSystemDecision)
                .user(userPrompt)
                .call()
                .content();
    }

    public Map<String, Object> filterRawData(String userPrompt, Long userId){
        String aiChoice = chatDesc(userPrompt);

        if (aiChoice.startsWith("Para kay Peep")){
            return checkIfTicketExist(userPrompt, userId);
        }

        if (aiChoice.startsWith("Para sa Admin")){
            deleteUserRequestIfExisted(userId);
            return checkIfTicketExist(userPrompt, userId);
        }
        return null;
    }


    private Map<String, Object> checkIfAdminIsOnline(String message, Long userId){
        ChatTicket ticket = new ChatTicket();
        if(!adminStatus.isAnyAdminOnline()){
              ticket.setUserId(userId);
              ticket.setInitialMessage(message);
              ticket.setCreatedAt(LocalDateTime.now());
              ticket.setStatus(TicketStatus.AI_RESPONSE);
              ticket.setOpenedAt(LocalDateTime.now());
              ticket = chatTicketRepository.save(ticket);
              saveMessage(ticket.getId(), userId, message, "USER");

            String aiResponse = chatClient
                    .prompt()
                    .system(peepSystemPrompt)
                    .user(message)
                    .call()
                    .content();

            return Map.of(
                    "response", aiResponse,
                    "answeredBy", "Peep",
                    "redirectToAdmin", false,
                    "ticketId", ticket.getId()
            );
        }

        ticket.setUserId(userId);
        ticket.setInitialMessage(message);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setOpenedAt(LocalDateTime.now());
        chatTicketRepository.save(ticket);

        return Map.of(
                "response", "Maghintay lang sandali, may kausap pa ang admin na miyembro. Ikaw ay nakapila na!",
                "answeredBy", "Peep",
                "redirectToAdmin", true,
                "ticketId", ticket.getId()
        );

    }

    private void deleteUserRequestIfExisted(Long userId){
        Optional<ChatTicket> existingTicket = chatTicketRepository.findByUserIdAndStatusIn(userId, List.of(TicketStatus.PENDING));
        if(existingTicket.isPresent()) {
            chatMessageRepository.deleteByTicketId(existingTicket.get().getId());
            chatTicketRepository.delete(existingTicket.get());
        }

    }

    private Map<String, Object> checkIfTicketExist(String message, Long userId){
        Optional<ChatTicket> existingTicket = chatTicketRepository.findByUserIdAndStatusIn(userId, List.of(TicketStatus.PENDING, TicketStatus.OPEN, TicketStatus.AI_RESPONSE));

        if (existingTicket.isEmpty()){
           return checkIfAdminIsOnline(message, userId);
        }

        saveMessage(existingTicket.get().getId(), userId, message, "USER");

       return null;
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
