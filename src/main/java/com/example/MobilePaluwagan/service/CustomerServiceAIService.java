package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.config.AdminStatusTracker;
import com.example.MobilePaluwagan.controller.SseController;
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
import java.util.*;
import java.util.stream.Collectors;


@Service
public class CustomerServiceAIService {
    private final ChatClient groqChatClient;
    private final ChatClient geminiChatClient;
    private final AdminStatusTracker adminStatus;
    private final ChatTicketRepository chatTicketRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient groqFallback2ChatClient;
    private final ChatClient groqFallback3ChatClient;
    private final ChatClient groqFallback4ChatClient;
    private final UserInfoRepo userInfoRepo;
    private final SseController sseController;

    @Value("classpath:prompts/peep-system-prompt.st")
    private Resource peepSystemPrompt;

    @Value("classpath:prompts/peep-subject-making.st")
    private Resource ticketSubjectPrompt;

    @Value("classpath:prompts/peep-decision.st")
    private Resource peepSystemDecision;


    public CustomerServiceAIService(
            @Qualifier("groqChatClient") ChatClient groqChatClient,
            @Qualifier("groqFallback2ChatClient") ChatClient groqFallback2ChatClient,
            @Qualifier("groqFallback3ChatClient") ChatClient groqFallback3ChatClient,
            @Qualifier("groqFallback4ChatClient") ChatClient groqFallback4ChatClient,
            @Qualifier("geminiChatClient") ChatClient geminiChatClient,
            AdminStatusTracker adminStatus,
            ChatTicketRepository chatTicketRepository,
            ChatMessageRepository chatMessageRepository,
            SseController sseController,
            UserInfoRepo userInfoRepo, SseController sseController1) {
        this.groqChatClient = groqChatClient;
        this.groqFallback2ChatClient = groqFallback2ChatClient;
        this.groqFallback3ChatClient = groqFallback3ChatClient;
        this.groqFallback4ChatClient = groqFallback4ChatClient;
        this.geminiChatClient = geminiChatClient;
        this.adminStatus = adminStatus;
        this.chatTicketRepository = chatTicketRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userInfoRepo = userInfoRepo;
        this.sseController = sseController1;
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

        // Try Fallback 2
        try {
            return groqFallback2ChatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            System.out.println("Fallback 2 Groq failed: " + e.getMessage());
        }

        // Try Fallback 3
        try {
            return groqFallback3ChatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            System.out.println("Fallback 3 Groq failed: " + e.getMessage());
        }

        // Try Fallback 4
        try {
            return groqFallback4ChatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            System.out.println("Fallback 4 Groq failed: " + e.getMessage());
        }

        // Last resort — Gemini
        try {
            return geminiChatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            System.out.println("Gemini failed: " + e.getMessage());
        }

        throw new RuntimeException("All AI providers failed. Please try again later.");
    }

    private String chatDesc(String userPrompt){
        return callAI(peepSystemDecision, userPrompt);
    }

    public Map<String, Object> filterRawData(String userPrompt, Long userId){
        String aiChoice = chatDesc(userPrompt);

        if (aiChoice.startsWith("Para kay Peep")){
            return checkIfTicketExist(userPrompt, userId, false);
        }

        else if (aiChoice.startsWith("Para sa Admin")){
            switchToAdminIfExisted(userId);
            return checkIfTicketExist(userPrompt, userId, true);
        }
        return null;
    }


    private Map<String, Object> checkIfAdminIsOnline(String message, Long userId, boolean forAdmin){
        ChatTicket ticket = new ChatTicket();

        // If for Peep lang — always si Peep, hindi na need i-check kung online ang admin
        if (!forAdmin) {
            String subject = callAI(ticketSubjectPrompt, message);
            ticket.setUserId(userId);
            ticket.setInitialMessage(subject);
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

        // For Admin — ngayon na lang nag-che-check kung online
        if (!adminStatus.isAnyAdminOnline()) {
            // Admin offline — si Peep muna
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

        // Admin online AND gusto ng user ang admin — PENDING
        String subject = callAI(ticketSubjectPrompt, message);
        ticket.setUserId(userId);
        ticket.setInitialMessage(subject);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setOpenedAt(LocalDateTime.now());
        ticket = chatTicketRepository.save(ticket);
        sseController.notifyAdminNewChatMessage(userId, message, ticket.getId());
        saveMessage(ticket.getId(), userId, message, "USER");

        return buildResponse(
                ticket.getId(), userId,
                "Maghintay lang ng sandali, may kausap pa ang admin na miyembro. Ikaw ay nakapila na!",
                "Peep",
                true,
                true
        );
    }




    @Transactional
    public void switchToAdminIfExisted(Long userId){
        Optional<ChatTicket> existingTicket = chatTicketRepository.findByUserIdAndStatusIn(
                userId, List.of(TicketStatus.AI_RESPONSE)
        );
        if(existingTicket.isPresent()) {
            ChatTicket ticket = existingTicket.get();
            ticket.setStatus(TicketStatus.PENDING);
            ticket.setCreatedAt(LocalDateTime.now()); // reset time para mapunta sa dulo ng queue
            chatTicketRepository.save(ticket);
        }
    }

    private Map<String, Object> checkIfTicketExist(String message, Long userId, boolean forAdmin){
        Optional<ChatTicket> existingTicket = chatTicketRepository.findByUserIdAndStatusIn(
                userId, List.of(TicketStatus.PENDING, TicketStatus.OPEN, TicketStatus.AI_RESPONSE)
        );

        if (existingTicket.isEmpty()){
            return checkIfAdminIsOnline(message, userId, forAdmin);
        }

        ChatTicket ticket = existingTicket.get();

        // PENDING — nakapila pa lang
        if (ticket.getStatus() == TicketStatus.PENDING) {
            saveMessage(ticket.getId(), userId, message, "USER");
            sseController.notifyAdminNewChatMessage(userId, message, ticket.getId());
            return buildResponse(
                    ticket.getId(), userId,
                    "Ang iyong mensahe ay naipadala na sa admin. Maghintay lang sandali!",
                    "Peep",
                    true,
                    true
            );
        }

//     OPEN — may admin na, huwag nang mag-respond si Peep
        if (ticket.getStatus() == TicketStatus.OPEN) {
            saveMessage(ticket.getId(), userId, message, "USER");
            sseController.notifyAdminNewChatMessage(userId, message, ticket.getId());
            return Map.of(
                    "ticketId", ticket.getId()
            );
        }

        // AI_RESPONSE — si Peep mag-respond
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

    private ChatMessage saveMessage(String ticketId, Long userId, String message, String sentBy) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTicketId(ticketId);
        chatMessage.setUserId(userId);
        chatMessage.setMessage(message);
        chatMessage.setSentBy(sentBy);
        chatMessage.setCreatedAt(LocalDateTime.now());
        return chatMessageRepository.save(chatMessage);
    }

    public ApiResponse<Map<String, Object>> ticketList(Long adminId) {
        List<ChatTicket> tickets = chatTicketRepository.findAll();

        if (tickets.isEmpty()) {
            return new ApiResponse<>(false, "No ticket found in the database", null);
        }

        List<UserInfo> users = userInfoRepo.findAll();

        // Current — yung kinuha ng admin na ito
        TicketListAdminResponse current = tickets.stream()
                .filter(ticket -> ticket.getClaimedBy() != null
                        && ticket.getClaimedBy().equals(adminId)
                        && ticket.getStatus().equals(TicketStatus.OPEN))
                .findFirst()
                .map(ticket -> mapToResponse(ticket, users))
                .orElse(null);

        // Pending — hindi pa na-claim ng kahit sino
        List<TicketListAdminResponse> pendingTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus().equals(TicketStatus.PENDING))
                .sorted(Comparator.comparing(ChatTicket::getCreatedAt))
                .map(ticket -> mapToResponse(ticket, users))
                .collect(Collectors.toList());

        TicketListAdminResponse next = pendingTickets.isEmpty() ? null : pendingTickets.get(0);
        List<TicketListAdminResponse> list = pendingTickets.isEmpty() ? List.of() : pendingTickets.subList(1, pendingTickets.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("current", current); // yung aktibong hawak ng admin
        result.put("next", next);
        result.put("list", list);

        return new ApiResponse<>(true, "Tickets retrieved successfully", result);
    }

    // Extract para hindi paulit-ulit
    private TicketListAdminResponse mapToResponse(ChatTicket ticket, List<UserInfo> users) {
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
    }

    private Map<String, Object> buildResponse(String ticketId, Long userId, String response, String answeredBy, boolean redirectToAdmin, boolean saveToDb) {
        if (saveToDb) {
            saveMessage(ticketId, userId, response, answeredBy); // or "SYSTEM" / "AI" sender
        }

        return Map.of(
                "response", response,
                "answeredBy", answeredBy,
                "redirectToAdmin", redirectToAdmin,
                "ticketId", ticketId
        );
    }
}
