package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.ChatTicket;
import com.example.MobilePaluwagan.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatTicketRepository extends JpaRepository<ChatTicket, String> {

    Optional<ChatTicket> findByUserIdAndStatusIn(Long userId, List<TicketStatus> status);
    Optional<ChatTicket> findFirstByStatusOrderByCreatedAtAsc(TicketStatus status);
    List<ChatTicket> findByStatusOrderByCreatedAtAsc(TicketStatus status);
    Optional<ChatTicket> findByUserIdAndStatus(Long userId, TicketStatus status);
}
