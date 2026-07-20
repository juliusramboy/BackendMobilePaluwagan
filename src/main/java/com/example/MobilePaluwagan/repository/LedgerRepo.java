package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.Description;
import com.example.MobilePaluwagan.entity.Ledger;
import com.example.MobilePaluwagan.entity.LoanPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface LedgerRepo extends JpaRepository<Ledger, Long> {

    Ledger findByUserId(Long userId);
    Page<Ledger> findAllByUserId(Long userId, Pageable pageable);

    void deleteByUserId(Long userId);

    @Query(value = "SELECT l.* FROM ledger l " +
            "WHERE l.user_id = :userId " +
            "AND (:reference IS NULL OR LOWER(l.reference) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
            "AND (:method IS NULL OR l.mode_of_payment = :method) " +
            "AND (:description IS NULL OR l.description = :description) " +
            "ORDER BY l.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM ledger l " +  // ← add this
                    "WHERE l.user_id = :userId " +
                    "AND (:reference IS NULL OR LOWER(l.reference) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
                    "AND (:method IS NULL OR l.mode_of_payment = :method) " +
                    "AND (:description IS NULL OR l.description = :description)",
            nativeQuery = true)
    Page<Ledger> findLedgerByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("reference") String reference,
            @Param("method") String method,
            @Param("description") String description,
            Pageable pageable
    );
}
