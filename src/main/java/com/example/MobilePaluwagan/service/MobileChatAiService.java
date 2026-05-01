package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Request.BorrowerNameRequest;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.UserFullLoanResponse;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.repository.DueDateScheduleRepository;
import com.example.MobilePaluwagan.repository.LoanPaymentRepo;
import com.example.MobilePaluwagan.repository.UserLoanRepo;
import com.example.MobilePaluwagan.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MobileChatAiService {

    private final UserLoanRepo userLoanRepo;
    private final DueDateScheduleRepository dueDateScheduleRepository;
    private final LoanPaymentRepo loanPaymentRepo;
    private final UserRepo userRepo;

    @Value("${internal.secret-key}")
    private String secretKey;


    public ApiResponse<UserFullLoanResponse> loanAllCredentials(BorrowerNameRequest request) {

        if(!request.getInternalKey().equals(secretKey)){
            return new ApiResponse<>(false, "Unauthorized", null);
        }


        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            return new ApiResponse<>(
                    false,
                    "Borrower name cannot be empty",
                    null
            );
        }

        UserFullLoanResponse response = userLoanRepo.findBorrowerByName(request.getFirstName());

        if (response == null) {
            return new ApiResponse<>(
                    false,
                    "No borrower found with name: " + request.getFirstName(),
                    null
            );
        }

        response.setDueDates(
                dueDateScheduleRepository.findUserListDueDatesByApplicationId(response.getApplicationId())
        );

        response.setPayments(
                loanPaymentRepo.findUserListPaymentsByLoanId(response.getId())
        );

        return new ApiResponse<>(
                true,
                "successfully get data from loan and savings",
                response
        );
    }


}
