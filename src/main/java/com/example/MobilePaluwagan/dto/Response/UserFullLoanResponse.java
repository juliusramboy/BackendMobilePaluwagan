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
public class UserFullLoanResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private Long applicationId;
    private BigDecimal balance;

    private List<UserListDueDates> dueDates;
    private List<UserListPayments> payments;

    public UserFullLoanResponse(Long id, String firstName, String lastName, Long applicationId, BigDecimal balance) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.applicationId = applicationId;
        this.balance = balance;
    }


}
