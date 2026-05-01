package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.dto.Request.BorrowerNameRequest;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.UserAllLoansResponse;
import com.example.MobilePaluwagan.dto.Response.UserFullLoanResponse;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.entity.UserPrinciple;
import com.example.MobilePaluwagan.repository.UserRepo;
import com.example.MobilePaluwagan.service.LoanService;
import com.example.MobilePaluwagan.service.MobileChatAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mobile/chat")
public class MobileAiController {
    private final MobileChatAiService mobileChatAiService;
    private final UserRepo userRepo;

    @PostMapping("/v1/user-loan")
    public ApiResponse<UserFullLoanResponse> getSpecificUserLoan(@RequestBody BorrowerNameRequest request){
        return mobileChatAiService.loanAllCredentials(request);
    }


}
