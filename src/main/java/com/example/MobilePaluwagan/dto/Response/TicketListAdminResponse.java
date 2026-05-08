package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketListAdminResponse {
    private String ticketId;
    private String name;
    private String problem;
    private LocalDateTime time;
}
