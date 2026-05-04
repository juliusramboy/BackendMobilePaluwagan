package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByTicketIdOrderByCreatedAtAsc(String ticketId);
    void deleteByTicketId(String ticketId);
    @Query("SELECT DISTINCT c.userId FROM ChatMessage c")
    List<Long> findDistinctUserIds();
    Optional<ChatMessage> findTopByTicketIdOrderByCreatedAtDesc(String ticketId);
}
