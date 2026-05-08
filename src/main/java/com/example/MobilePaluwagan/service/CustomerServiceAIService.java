package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.config.AdminStatusTracker;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.TicketListAdminResponse;
import com.example.MobilePaluwagan.entity.ChatMessage;
import com.example.MobilePaluwagan.entity.ChatTicket;
import com.example.MobilePaluwagan.entity.TicketStatus;
import com.example.MobilePaluwagan.entity.UserInfo;
import com.example.MobilePaluwagan.repository.ChatMessageRepository;
import com.example.MobilePaluwagan.repository.ChatTicketRepository;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import jakarta.transaction.Transactional;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class CustomerServiceAIService {
    private final ChatClient groqChatClient;
    private final ChatClient geminiChatClient;
    private final AdminStatusTracker adminStatus;
    private final ChatTicketRepository chatTicketRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient groqFallbackChatClient;
    private final UserInfoRepo userInfoRepo;

    @Value("classpath:prompts/peep-system-prompt.st")
    private Resource peepSystemPrompt;

    @Value("classpath:prompts/peep-decision.st")
    private Resource peepSystemDecision;


    public CustomerServiceAIService(
            @Qualifier("groqChatClient") ChatClient groqChatClient,
            @Qualifier("groqFallbackChatClient") ChatClient groqFallbackChatClient,
            @Qualifier("geminiChatClient") ChatClient geminiChatClient,
            AdminStatusTracker adminStatus,
            ChatTicketRepository chatTicketRepository,
            ChatMessageRepository chatMessageRepository,
            UserInfoRepo userInfoRepo) {
        this.groqChatClient = groqChatClient;
        this.groqFallbackChatClient = groqFallbackChatClient;
        this.geminiChatClient = geminiChatClient;
        this.adminStatus = adminStatus;
        this.chatTicketRepository = chatTicketRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userInfoRepo = userInfoRepo;
    }

    private String callAI(Resource systemPrompt, String userMessage) {
        // Try Primary Groq
        try {
            return groqChatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            System.out.println("Primary Groq failed: " + e.getMessage());
        }

        // Try Fallback Groq
            return groqFallbackChatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();


    }

    private String chatDesc(String userPrompt){
        return callAI(peepSystemDecision, userPrompt);
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


            String aiResponse = callAI(peepSystemPrompt, message);

            saveMessage(ticket.getId(), userId, aiResponse, "Peep");

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
        ticket = chatTicketRepository.save(ticket);

        return Map.of(
                "response", "Maghintay lang sandali, may kausap pa ang admin na miyembro. Ikaw ay nakapila na!",
                "answeredBy", "Peep",
                "redirectToAdmin", true,
                "ticketId", ticket.getId()
        );

    }

    @Transactional
    public void deleteUserRequestIfExisted(Long userId){
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

        String aiResponse = callAI(peepSystemPrompt, message);

          saveMessage(existingTicket.get().getId(), userId, aiResponse, "Peep");

        return Map.of(
                "response", aiResponse,
                "answeredBy", "Peep",
                "redirectToAdmin", false
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

    public ApiResponse<Map<String, Object>> ticketList() {
        List<ChatTicket> tickets = chatTicketRepository.findAll();

        if (tickets.isEmpty()) {
            return new ApiResponse<>(
                    false,
                    "No ticket found in the database",
                    null
            );
        }

        List<UserInfo> users = userInfoRepo.findAll();

        // Filter PENDING only and map to response
        List<TicketListAdminResponse> pendingTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus().equals("PENDING"))
                .map(ticket -> {
                    UserInfo matchedUser = users.stream()
                            .filter(u -> u.getId().equals(ticket.getUserId()))
                            .findFirst()
                            .orElse(null);

                    String fullName = matchedUser != null
                            ? matchedUser.getFirstName() + " " + matchedUser.getLastName()
                            : "Unknown";

                    return new TicketListAdminResponse(
                            ticket.getId(),
                            fullName,
                            ticket.getInitialMessage(),
                            ticket.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        // Separate next (first in queue) from the rest
        TicketListAdminResponse next = pendingTickets.isEmpty() ? null : pendingTickets.get(0);
        List<TicketListAdminResponse> list = pendingTickets.isEmpty() ? List.of() : pendingTickets.subList(1, pendingTickets.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("next", next);
        result.put("list", list);

        return new ApiResponse<>(
                true,
                "Pending tickets retrieved successfully",
                result
        );
    }
}
