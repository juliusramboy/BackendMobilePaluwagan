package com.example.MobilePaluwagan.dto.Response;

import com.example.MobilePaluwagan.entity.DueDateSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFullLoanResponse {
    private Long id;
    private String firstName;
    private Long applicationId;
    private BigDecimal balance;
    private List<UserListDueDates> dueDates;
    private List<UserListPayments> payments;

    public UserFullLoanResponse(Long id, String firstName, Long applicationId, BigDecimal balance) {
        this.id = id;
        this.firstName = firstName;
        this.applicationId = applicationId;
        this.balance = balance;
    }


}
