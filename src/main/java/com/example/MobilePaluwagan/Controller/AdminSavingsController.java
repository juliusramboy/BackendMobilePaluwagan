package com.example.MobilePaluwagan.Controller;

import com.example.MobilePaluwagan.DTOs.Request.UserDepositRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin/savings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSavingsController {

//    @PostMapping("/deposit")
//    public ResponseEntity<ApiResponse<?>> deposit(@RequestBody UserDepositRequest request){
//        return;
//    }

}
