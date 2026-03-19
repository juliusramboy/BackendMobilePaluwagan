package com.example.MobilePaluwagan.dto.Response;

import com.example.MobilePaluwagan.entity.Ledger;
import com.example.MobilePaluwagan.entity.LoanPayment;
import com.example.MobilePaluwagan.entity.UserSavings;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class AdminMemberListResponse {
    private UserProfileResponse info;
    private Page<Ledger> allPayments;

}
