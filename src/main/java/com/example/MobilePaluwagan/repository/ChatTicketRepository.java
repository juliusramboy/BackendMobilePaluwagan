package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.ChatTicket;
import com.example.MobilePaluwagan.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatTicketRepository extends JpaRepository<ChatTicket, String> {

    Optional<ChatTicket> findByUserIdAndStatusIn(Long userId, List<TicketStatus> status);
    Optional<ChatTicket> findFirstByStatusOrderByCreatedAtAsc(TicketStatus status);
    List<ChatTicket> findByStatusOrderByCreatedAtAsc(TicketStatus status);
    Optional<ChatTicket> findByUserIdAndStatus(Long userId, TicketStatus status);
    List<ChatTicket> findByStatusAndOpenedAtBefore(TicketStatus status, LocalDateTime cutoff);
    List<ChatTicket> findByStatusIn(List<TicketStatus> statuses);
    List<ChatTicket> findByStatusAndClosedAtBefore(TicketStatus status, LocalDateTime cutoff);
    Optional<ChatTicket> findByUserId(Long userId);

    boolean existsByClaimedByAndStatusIn(Long adminId, List<TicketStatus> statuses);
}
