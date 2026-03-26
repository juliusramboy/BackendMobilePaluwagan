package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.Ledger;
import com.example.MobilePaluwagan.entity.LoanPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepo extends JpaRepository<Ledger, Long> {

    Ledger findByUserId(Long userId);
    Page<Ledger> findAllByUserId(Long userId, Pageable pageable);

    void deleteByUserId(Long userId);
}
