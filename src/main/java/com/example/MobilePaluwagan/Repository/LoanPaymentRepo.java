package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.LoanPayment;
import com.example.MobilePaluwagan.Entity.UserBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanPaymentRepo extends JpaRepository<LoanPayment, Long> {
    Optional<LoanPayment> findByUserId(Long userId);

    List<LoanPayment> findAllByUserId(Long userId);
    List<LoanPayment> findAllById(Long loanId);

    @Query(value = "SELECT lp.* FROM loan_payment lp " +
            "JOIN loan l ON lp.loan_id = l.id " +
            "WHERE l.user_id = :userId",
            nativeQuery = true)
    List<LoanPayment> findPaymentsByUserIdNative(@Param("userId") Long userId);
}
