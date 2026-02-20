package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.DTOs.Request.PaymentFilterRequest;
import com.example.MobilePaluwagan.DTOs.Response.SavingsApplicantsAdmin;
import com.example.MobilePaluwagan.Entity.Status;
import com.example.MobilePaluwagan.Entity.UserSavings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserSavingsRepo extends JpaRepository<UserSavings, Long> {

    List<UserSavings> findByUserId(long userId);
    Long countByUserId(long userId);
    boolean existsByUserIdAndStatus(long userId, Status status);


    @Query("SELECT e FROM UserSavings e ORDER BY e.id desc LIMIT 1")
    Optional<UserSavings> findLastRef();

//    @Query(value = "SELECT us.* FROM user_savings us " +
//            "WHERE us.user_id = :userId " +
//            "AND (:reference IS NULL OR LOWER(us.reference) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
//            "AND (:startDate IS NULL OR DATE(us.deposit_date) >= :startDate) " +
//            "AND (:endDate IS NULL OR DATE(us.deposit_date) <= :endDate) " +
//            "AND (:status IS NULL OR us.status = :status) " +
//            "ORDER BY us.deposit_date DESC",
//            nativeQuery = true)
//    List<UserSavings> findPaymentsByUserIdWithFilters(
//            @Param("userId") Long userId,
//            @Param("reference") String reference,
//            @Param("startDate")LocalDate startDate,
//            @Param("endDate")LocalDate endDate,
//            @Param("status") String status
//            );

    @Query(value = "SELECT us.* FROM user_savings us " +
            "WHERE us.user_id = :userId " +
            "AND (:reference IS NULL OR LOWER(us.reference) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
            "AND (CAST(:startDate AS date) IS NULL OR DATE(us.deposit_date) >= CAST(:startDate AS date)) " +
            "AND (CAST(:endDate AS date) IS NULL OR DATE(us.deposit_date) <= CAST(:endDate AS date)) " +
            "AND (:status IS NULL OR us.status = :status) " +
            "ORDER BY us.deposit_date DESC",
            nativeQuery = true)
    List<UserSavings> findPaymentsByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("reference") String reference,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status
    );

    @Query("SELECT new com.example.MobilePaluwagan.DTOs.Response.SavingsApplicantsAdmin(" +
    "us.firstName, us.lastName, sa.amountDeposit, sa.depositDate, sa.status, sa.reference) " +
    "FROM UserSavings sa " +
    "JOIN sa.userInfo us " +
    "WHERE sa.status = :status")
    List<SavingsApplicantsAdmin> findByLoanStatus(@Param("status") Status status);


}
