package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepo extends JpaRepository<Ledger, Long> {

    Ledger findByUserId(Long userId);
}
