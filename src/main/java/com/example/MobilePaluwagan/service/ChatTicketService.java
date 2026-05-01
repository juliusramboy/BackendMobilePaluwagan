package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.config.AdminStatusTracker;
import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.entity.ChatMessage;
import com.example.MobilePaluwagan.entity.ChatTicket;
import com.example.MobilePaluwagan.entity.TicketStatus;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.repository.ChatMessageRepository;
import com.example.MobilePaluwagan.repository.ChatTicketRepository;
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

//    @Transactional
//    public Map<String, Object> requestChat(String message, Long userId) {
//
//        Optional<ChatTicket> existingTicket = chatTicketRepository.findByUserIdAndStatusIn(userId, List.of(TicketStatus.PENDING, TicketStatus.OPEN));
//
//        if(existingTicket.isPresent()) {
//            chatMessageRepository.deleteByTicketId(existingTicket.get().getId());
//            chatTicketRepository.delete(existingTicket.get());
//            System.out.println("Old ticket deleted for userId: " + userId);
//        }
//
//        if (!adminStatusTracker.isAnyAdminOnline()){
//            String aiResponse = customerServiceAIService.chat(message);
//            if (aiResponse.startsWith("CANNOT_ANSWER:")) {
//                aiResponse = aiResponse.replace("CANNOT_ANSWER:", "").trim();
//            }
//            return Map.of(
//                    "response", aiResponse,
//                    "answeredBy", "Peps",
//                    "redirectToAdmin", false
//            );
//        }
//
//        ChatTicket ticket = new ChatTicket();
//        String aiResponse = customerServiceAIService.chat(message);
//
//        User user = userRepo.findById(userId).orElseThrow();
//
//        if (user.isAwaitingTicketConfirmation()) {
//            user.setAwaitingTicketConfirmation(false);
//            userRepo.save(user);
//
//            if (message.equalsIgnoreCase("yes")) {
//                ticket.setUserId(userId);
//                ticket.setInitialMessage(message);
//                ticket.setCreatedAt(LocalDateTime.now());
//                ticket.setStatus(TicketStatus.OPEN);
//                ticket.setOpenedAt(LocalDateTime.now());
//                chatTicketRepository.save(ticket);
//                saveMessage(ticket.getId(), userId, message, "USER");
//                sseController.notifyAdminNewChatMessage(userId, message, ticket.getId());
//
//                return Map.of(
//                        "response", "Ticket created! An admin will assist you shortly.",
//                        "ticketId", ticket.getId(),
//                        "status", "OPEN",
//                        "redirectToAdmin", true
//                );
//            } else {
//                return Map.of(
//                        "response", "Okay! Kung may iba ka pang tanong, nandito lang ako.",
//                        "answeredBy", "Peps",
//                        "redirectToAdmin", false
//                );
//            }
//        }
//
//        if (aiResponse.startsWith("CANNOT_ANSWER:")) {
//            user.setAwaitingTicketConfirmation(true); // set flag
//            userRepo.save(user);
//
//            String cleanResponse = aiResponse.replace("CANNOT_ANSWER:", "").trim();
//            return Map.of(
//                    "response", cleanResponse,
//                    "redirectToAdmin", false
//            );
//        }
//
//        // AI can't answer — redirect to admin regardless of queue
//        if (aiResponse.startsWith("CANNOT_ANSWER:") ) {
//            if (message.equalsIgnoreCase("yes")){
//                String cleanResponse = aiResponse.replace("CANNOT_ANSWER:", "").trim();
//
//                ticket.setUserId(userId);
//                ticket.setInitialMessage(message);
//                ticket.setCreatedAt(LocalDateTime.now());
//                ticket.setStatus(TicketStatus.OPEN);
//                ticket.setOpenedAt(LocalDateTime.now());
//                chatTicketRepository.save(ticket);
//                saveMessage(ticket.getId(), userId, message, "USER");
//                sseController.notifyAdminNewChatMessage(userId, message, ticket.getId());
//
//                return Map.of(
//                        "response", cleanResponse,
//                        "ticketId", ticket.getId(),
//                        "status", "OPEN",
//                        "redirectToAdmin", true
//                );
//            }else {
//                return Map.of(
//                        "response", "Okay! Kung may iba ka pang tanong, nandito lang ako.",
//                        "answeredBy", "Peps",
//                        "redirectToAdmin", false
//                );
//            }
//
//        }
//
//        Optional<ChatTicket> openTicket = chatTicketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.OPEN);
//
//        ticket.setUserId(userId);
//        ticket.setInitialMessage(message);
//        ticket.setCreatedAt(LocalDateTime.now());
//
//        if (openTicket.isEmpty()) {
//            ticket.setStatus(TicketStatus.OPEN);
//            ticket.setOpenedAt(LocalDateTime.now());
//            chatTicketRepository.save(ticket);
//
//            saveMessage(ticket.getId(), userId, message, "USER");
//
//            sseController.notifyAdminNewChatMessage(userId, message, ticket.getId());
//
//            return Map.of(
//                    "message", "Maghintay ng ilang minuto kinokonect ka na sa admin.",
//                    "ticketId", ticket.getId(),
//                    "status", "OPEN",
//                    "redirectToAdmin", true
//            );
//        } else {
//            ticket.setStatus(TicketStatus.PENDING);
//            chatTicketRepository.save(ticket);
//
//            List<ChatTicket> pendingTicket = chatTicketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.PENDING);
//            int position = pendingTicket.size();
//
//
//            return Map.of(
//                    "message", "May Kausap pa ang admin na Myembro nawa'y maghintay ng ilang minuto. " +
//                            "Ikaw ay #" + position + " sa pila. " +
//                            "Pakihintay.",
//                    "ticketId", ticket.getId(),
//                    "status", "PENDING",
//                    "queuePosition", position
//            );
//        }
//    }

    @Transactional
    public Map<String, Object> requestChat(String message, Long userId) {

        // ============================================================
        // STEP 1: Check if user is waiting for yes/no confirmation
        // This must be FIRST before anything else runs so that
        // the user's "yes" or "no" reply is handled correctly
        // ============================================================
        User user = userRepo.findById(userId).orElseThrow();

        if (user.isAwaitingTicketConfirmation()) {

            System.out.println("=== STEP 1 TRIGGERED for userId: " + userId + " message: " + message);

            // Clear the flag regardless of their answer
            user.setAwaitingTicketConfirmation(false);
            userRepo.saveAndFlush(user);

            // Find the DRAFT ticket that was created when AI couldn't answer
            // This holds the original question as initialMessage
            ChatTicket draftTicket = chatTicketRepository
                    .findByUserIdAndStatus(userId, TicketStatus.DRAFT)
                    .orElseThrow(() -> new RuntimeException("DRAFT ticket not found for userId: " + userId));

            if (message.equalsIgnoreCase("yes")) {

                // Check the queue — is any ticket currently OPEN?
                Optional<ChatTicket> openTicket = chatTicketRepository
                        .findFirstByStatusOrderByCreatedAtAsc(TicketStatus.OPEN);

                if (openTicket.isEmpty()) {
                    // No active ticket being handled — promote DRAFT to OPEN immediately
                    draftTicket.setStatus(TicketStatus.OPEN);
                    draftTicket.setOpenedAt(LocalDateTime.now());
                    chatTicketRepository.saveAndFlush(draftTicket);

                    // Notify admin with the original question (not "yes")
                    sseController.notifyAdminNewChatMessage(userId, draftTicket.getInitialMessage(), draftTicket.getId());

                    return Map.of(
                            "response", "Ticket created! An admin will assist you shortly.",
                            "ticketId", draftTicket.getId(),
                            "status", "OPEN",
                            "redirectToAdmin", true
                    );
                } else {
                    // Admin is busy — promote DRAFT to PENDING and add to queue
                    draftTicket.setStatus(TicketStatus.PENDING);
                    chatTicketRepository.saveAndFlush(draftTicket);

                    List<ChatTicket> pendingTickets = chatTicketRepository
                            .findByStatusOrderByCreatedAtAsc(TicketStatus.PENDING);
                    int position = pendingTickets.size();

                    return Map.of(
                            "message", "May Kausap pa ang admin na Myembro nawa'y maghintay ng ilang minuto. " +
                                    "Ikaw ay #" + position + " sa pila. " +
                                    "Pakihintay.",
                            "ticketId", draftTicket.getId(),
                            "status", "PENDING",
                            "queuePosition", position
                    );
                }

            } else {
                // User said no — delete the draft ticket and its saved messages
                chatMessageRepository.deleteByTicketId(draftTicket.getId());
                chatTicketRepository.delete(draftTicket);

                return Map.of(
                        "response", "Okay! Kung may iba ka pang tanong, nandito lang ako.",
                        "answeredBy", "Peps",
                        "redirectToAdmin", false
                );
            }
        }

        if (user.isAwaitingAdminQuestion()) {

            // Clear the flag
            user.setAwaitingAdminQuestion(false);

            // NOW create the DRAFT ticket with their actual question
            ChatTicket dTicket = new ChatTicket();
            dTicket.setUserId(userId);
            dTicket.setInitialMessage(message); // ← actual question na ito
            dTicket.setCreatedAt(LocalDateTime.now());
            dTicket.setStatus(TicketStatus.DRAFT);
            chatTicketRepository.saveAndFlush(dTicket);

            // Save the actual question as a message
            saveMessage(dTicket.getId(), userId, message, "USER");

            // Now ask yes/no confirmation
            user.setAwaitingTicketConfirmation(true);
            userRepo.saveAndFlush(user);

            return Map.of(
                    "response", "Mayroon ka nang aktibong ticket. Maghintay lang habang tinutulungan ka ng admin.",
                    "ticketId", dTicket.getId(),
                    "status", "OPEN",
                    "redirectToAdmin", true
            );
        }

        // ============================================================
        // STEP 2: Check if user already has an active ticket
        // DRAFT/OPEN = block (already in process)
        // PENDING = allow, delete old and re-queue with new message
        // ============================================================
        Optional<ChatTicket> existingTicket = chatTicketRepository.findByUserIdAndStatusIn(
                userId, List.of(TicketStatus.DRAFT, TicketStatus.PENDING, TicketStatus.OPEN)
        );

        if (existingTicket.isPresent()) {
            ChatTicket existing = existingTicket.get();

            if (existing.getStatus() == TicketStatus.OPEN) {
                // Already being served by admin — block
                return Map.of(
                        "response", "Mayroon ka nang aktibong ticket. Maghintay lang habang tinutulungan ka ng admin.",
                        "ticketId", existing.getId(),
                        "status", "OPEN",
                        "redirectToAdmin", true
                );
            }

            if (existing.getStatus() == TicketStatus.DRAFT) {
                // Still waiting for yes/no but flag was lost somehow — block
                return Map.of(
                        "response", "Pakisagot muna ang iyong nakabitin na tanong bago mag-send ng bago.",
                        "ticketId", existing.getId(),
                        "status", "DRAFT",
                        "redirectToAdmin", false
                );
            }

            // Status is PENDING — delete old pending ticket so they can re-queue
            chatMessageRepository.deleteByTicketId(existing.getId());
            chatTicketRepository.delete(existing);
            System.out.println("Old PENDING ticket replaced for userId: " + userId);
        }

        // ============================================================
        // STEP 3: If no admin is online, let AI handle it
        // No ticket will be created in this case
        // ============================================================
        if (!adminStatusTracker.isAnyAdminOnline()) {
            String aiResponse = customerServiceAIService.chat(message);

            // Strip CANNOT_ANSWER prefix if present — just show AI's cleaned message
            if (aiResponse.startsWith("CANNOT_ANSWER:")) {
                aiResponse = aiResponse.replace("CANNOT_ANSWER:", "").trim();
            }

            return Map.of(
                    "response", aiResponse,
                    "answeredBy", "Peps",
                    "redirectToAdmin", false
            );
        }

        // ============================================================
        // STEP 3.5: User explicitly wants to talk to admin
        // Skip AI entirely — create DRAFT and ask for confirmation
        // ============================================================
        List<String> adminKeywords = List.of(
                "makausap ang admin",
                "makausap admin",
                "kausapin ang admin",
                "kausap ang admin",
                "i-connect sa admin",
                "talk to admin",
                "speak to admin",
                "human agent",
                "live agent",
                "tao gusto ko",
                "ayaw ko sa bot",
                "gusto ko ng tao",
                "pwede ba makausap",
                "may makausap ba ako",
                "admin please",
                "paki connect"
        );

        boolean wantsAdmin = adminKeywords.stream()
                .anyMatch(message.toLowerCase()::contains);

        if (wantsAdmin) {
            // Create DRAFT ticket to hold the original message

            user.setAwaitingAdminQuestion(true);
            userRepo.saveAndFlush(user);
//            ChatTicket draftTicket = new ChatTicket();
//            draftTicket.setUserId(userId);
//            draftTicket.setInitialMessage(message);
//            draftTicket.setCreatedAt(LocalDateTime.now());
//            draftTicket.setStatus(TicketStatus.DRAFT);
//            chatTicketRepository.saveAndFlush(draftTicket);
//
//            // Save original message under the draft ticket
//            saveMessage(draftTicket.getId(), userId, message, "USER");
//
//            // Set flag so next message (yes/no) is caught by Step 1
//            user.setAwaitingTicketConfirmation(true);
//            userRepo.saveAndFlush(user);

            return Map.of(
                    "response", "Sige! ano ang katanungan mo sa admin?",
                    "redirectToAdmin", false
            );
        }



        // ============================================================
        // STEP 4: Admin is online — ask AI if it can answer
        // If AI can't answer, create a DRAFT ticket to hold the
        // original question and prompt user to confirm
        // ============================================================
        String aiResponse = customerServiceAIService.chat(message);

        if (aiResponse.startsWith("CANNOT_ANSWER:")) {

            // Create a DRAFT ticket to hold the original question
//            ChatTicket draftTicket = new ChatTicket();
//            draftTicket.setUserId(userId);
//            draftTicket.setInitialMessage(message);
//            draftTicket.setCreatedAt(LocalDateTime.now());
//            draftTicket.setStatus(TicketStatus.DRAFT);
//            chatTicketRepository.saveAndFlush(draftTicket);
//
//            // Save the original question as a message under the draft ticket
//            saveMessage(draftTicket.getId(), userId, message, "USER");
//
//            // Set flag so the next message (yes/no) is handled in Step 1
//            user.setAwaitingTicketConfirmation(true);
//            userRepo.saveAndFlush(user);

            // Return AI's cleaned response to the user
            String cleanResponse = aiResponse.replace("CANNOT_ANSWER:", "").trim();
            return Map.of(
                    "response", cleanResponse,
                    "redirectToAdmin", false
            );
        }

        // ============================================================
        // STEP 5: AI answered normally — no ticket needed
        // ============================================================
        return Map.of(
                "response", aiResponse,
                "answeredBy", "Peps",
                "redirectToAdmin", false
        );
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
