package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.StatusResponse;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserStatusController {

    @Autowired
    private StatusService statusService;

    @GetMapping("/status")
    public ApiResponse<StatusResponse> checkLoanStatus(Authentication authentication) {
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        StatusResponse status = statusService.getUserStatus(userId);

        return new ApiResponse<>(true, "Success", status);
    }
}
