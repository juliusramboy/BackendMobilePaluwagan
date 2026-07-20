package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Request.LedgerFilterRequest;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.LedgerInfo;
import com.example.MobilePaluwagan.dto.Response.PaymentInfo;
import com.example.MobilePaluwagan.entity.Ledger;
import com.example.MobilePaluwagan.entity.LoanPayment;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.repository.LedgerRepo;
import com.example.MobilePaluwagan.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LedgerService {

    @Autowired
    private LedgerRepo ledgerRepo;
    @Autowired
    private UserRepo userRepo;




    public Page<Ledger> filterUserLedger(LedgerFilterRequest filter, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        return ledgerRepo.findLedgerByUserIdWithFilters(
                filter.getUserId(),
                filter.getReference(),
                filter.getMethod() != null ? filter.getMethod().name() : null,
                filter.getDescription() != null ? filter.getDescription().name() : null,
                pageable
        );
    }

    public LedgerInfo convertToDTO(Ledger payment) {
        return LedgerInfo.builder()
                .amount(payment.getAmount())
                .depositDate(payment.getDepositDate())
                .reference(payment.getReference())
                .description(payment.getDescription())
                .paymentMethod(payment.getModeOfPayment())
                .build();
    }

}
