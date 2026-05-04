package com.example.MobilePaluwagan.dto.Response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private String ticketId;
    private Long userId;
    private String message;
    private String sentBy;
    private LocalDateTime createdAt;
    private String senderName;
    private String senderProfileImage;
}
