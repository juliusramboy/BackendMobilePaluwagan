package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.LoanPayment;
import com.example.MobilePaluwagan.Entity.UserBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanPaymentRepo extends JpaRepository<LoanPayment, Long> {
    Optional<LoanPayment> findByUserId(Long userId);

    List<LoanPayment> findAllByUserId(Long userId);
}
