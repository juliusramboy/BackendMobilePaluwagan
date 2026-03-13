package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.Entity.Ledger;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Repository.LedgerRepo;
import com.example.MobilePaluwagan.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LedgerService {

    @Autowired
    private LedgerRepo ledgerRepo;

    @Autowired
    private UserRepo userRepo;


    public ApiResponse<?> getUserLedger(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Ledger ledger = ledgerRepo.findByUserId(user.getId());

        return null;
    }

}
