package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.entity.ChatMessage;
import com.example.MobilePaluwagan.entity.ChatTicket;
import com.example.MobilePaluwagan.entity.TicketStatus;
import com.example.MobilePaluwagan.repository.ChatMessageRepository;
import com.example.MobilePaluwagan.repository.ChatTicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketCleanupService {

    private final ChatTicketRepository chatTicketRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Scheduled(fixedRate = 20 * 60 * 1000)
    @Transactional
    public void markInactiveTicketsAsClosed() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(20);

        List<ChatTicket> tickets = chatTicketRepository
                .findByStatusIn(List.of(TicketStatus.AI_RESPONSE));

        for (ChatTicket ticket : tickets) {
            Optional<ChatMessage> lastMessage = chatMessageRepository
                    .findTopByTicketIdOrderByCreatedAtDesc(ticket.getId());

            boolean isInactive = lastMessage
                    .map(msg -> msg.getCreatedAt().isBefore(cutoff))
                    .orElse(true);

            if (isInactive) {
                ticket.setStatus(TicketStatus.CLOSED);
                ticket.setClosedAt(LocalDateTime.now());
                chatTicketRepository.save(ticket);
            }
        }
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    @Transactional
    public void deleteClosedTickets() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);

        List<ChatTicket> closedTickets = chatTicketRepository
                .findByStatusAndClosedAtBefore(TicketStatus.CLOSED, cutoff);

        for (ChatTicket ticket : closedTickets) {
            chatMessageRepository.deleteByTicketId(ticket.getId());
            chatTicketRepository.delete(ticket);
        }
    }
}
