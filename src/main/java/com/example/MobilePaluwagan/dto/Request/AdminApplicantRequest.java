package com.example.MobilePaluwagan.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminApplicantRequest {
    private Long applicationId;
    private Boolean status;
}
