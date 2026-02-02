package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.LoanPayment;
import com.example.MobilePaluwagan.Entity.UserBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

//    @Query("SELECT lp FROM LoanPayment lp WHERE " +
//            "(:reference IS NULL OR LOWER(lp.referenceNumber) LIKE LOWER(CONCAT('%', :reference, '%'))) AND " +
//            "((:startDate IS NULL OR :endDate IS NULL) OR lp.paymentDate BETWEEN :startDate AND :endDate) AND " +
//            "(:status IS NULL OR LOWER(lp.status) LIKE LOWER(CONCAT('%', :status, '%'))) AND " +
//            "(:paymentMethod IS NULL OR LOWER(lp.paymentMethod) LIKE LOWER(CONCAT('%', :paymentMethod, '%')))")
//    List<LoanPayment> findByFilters(
//            @Param("reference") String reference,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate,
//            @Param("status") String status,
//            @Param("paymentMethod") String paymentMethod
//    );

    @Query(value = "SELECT lp.* FROM loan_payment lp " +
            "JOIN loan l ON lp.loan_id = l.id " +
            "WHERE l.user_id = :userId " +
            "AND (:reference IS NULL OR LOWER(lp.reference_number) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
            "AND (:startDate IS NULL OR lp.payment_date >= :startDate) " +
            "AND (:endDate IS NULL OR lp.payment_date <= :endDate) " +
            "AND (:status IS NULL OR lp.status = :status) " +
            "AND (:paymentMethod IS NULL OR lp.payment_method = :paymentMethod) " +
            "ORDER BY lp.payment_date DESC",
            nativeQuery = true)
    List<LoanPayment> findPaymentsByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("reference") String reference,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status,
            @Param("paymentMethod") String paymentMethod
    );



}
