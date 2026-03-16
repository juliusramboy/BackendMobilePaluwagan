package com.example.MobilePaluwagan.controller;


import com.example.MobilePaluwagan.dto.Response.EnumDTOResponse;
import com.example.MobilePaluwagan.service.ReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/reference")
public class ReferenceDataController {

    @Autowired
    ReferenceDataService referenceDataService;

    @GetMapping("/payment-method")
    public ResponseEntity<List<EnumDTOResponse>> getPaymentUser() {
        List<EnumDTOResponse> response = referenceDataService.getPaymentMethodsUserAndAdmin();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .body(response);
    }


    @GetMapping("/status")
    public ResponseEntity<List<EnumDTOResponse>> getStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok(referenceDataService.getFilterStatusAdmin());
        }

        return ResponseEntity.ok(referenceDataService.getFilterStatusUser());
    }

}
