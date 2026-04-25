package com.example.MobilePaluwagan.dto.Response;

import com.example.MobilePaluwagan.entity.Ledger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMemberLedger {
    private Page<Ledger> allPayments;
}
