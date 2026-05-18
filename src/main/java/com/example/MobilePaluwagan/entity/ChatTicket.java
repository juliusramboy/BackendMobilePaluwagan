package com.example.MobilePaluwagan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_ticket")
@NoArgsConstructor
@AllArgsConstructor
public class ChatTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "initial_message")
    private String initialMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "claimed_by")
    private Long claimedBy;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    private boolean awaitingTicketConfirmation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;
}
