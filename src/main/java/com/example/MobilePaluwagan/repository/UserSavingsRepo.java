package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.dto.Response.SavingsMemberOverview;
import com.example.MobilePaluwagan.entity.Status;
import com.example.MobilePaluwagan.entity.UserSavings;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserSavingsRepo extends JpaRepository<UserSavings, Long> {

    List<UserSavings> findByUserId(long userId);
    Long countByUserId(long userId);
    boolean existsByUserIdAndStatus(long userId, Status status);
    boolean existsByReference(String reference);
    List<UserSavings> findBySavingsId(String savingsId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSavings us WHERE us.savingsId = :savingsId AND us.status = 'PENDING'")
    void deleteBySavingsIdAndStatus(@Param("savingsId") String savingsId);

   Page<UserSavings> findAllByUserIdAndStatusOrderByDepositDateDesc(Long  userId, Status status, Pageable pageable);


    @Query("SELECT SUM(us.amountDeposit) FROM UserSavings us")
    BigDecimal sumAllDeposits();

    @Query("SELECT COUNT(us) FROM UserSavings us WHERE us.status = 'PENDING'")
    int countAllPendingDeposits();


    @Query("SELECT e FROM UserSavings e ORDER BY e.id desc LIMIT 1")
    Optional<UserSavings> findLastRef();

    @Query("SELECT CAST(SUM(us.amountDeposit) AS java.math.BigDecimal)  FROM UserSavings us WHERE us.userId = :userId AND us.status = 'PAID'")
    BigDecimal sumAllPaidByUserId(@Param("userId") Long userId);


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

    @Query("SELECT new com.example.MobilePaluwagan.dto.Response.SavingsMemberOverview(" +
    "ui.firstName, ui.lastName, ub.savingsId, ub.accountBalance, ub.targetAmount, " +
    "CASE WHEN COUNT(us) > 0 THEN true ELSE false END, " +
    "CASE WHEN COUNT(sw) > 0 THEN true ELSE false END" +
    ",ui.profileImage) " +
    "FROM User u " +
    "JOIN u.userInfo ui " +
    "JOIN u.userBank ub " +
    "LEFT JOIN UserSavings us ON us.userId = u.id AND  us.status = 'PENDING' " +
    "LEFT JOIN SavingsWithdrawApplication sw ON u.id = sw.userId AND sw.status = 'WITHDRAW' " +
    "WHERE u.hasSavingsAccount = true " +
    "GROUP BY u.id, ui.firstName, ui.lastName, ub.savingsId, ub.accountBalance, ub.targetAmount, ui.profileImage " +
    "ORDER BY " +
    "CASE WHEN COUNT(us) > 0 OR COUNT(sw) > 0 THEN 0 ELSE 1 END ASC")
    List<SavingsMemberOverview> findAllMembers();

    @Query(value = "SELECT us.* FROM user_savings us " +
            "WHERE us.savings_id = :savingsId " +
            "AND (:reference IS NULL OR LOWER(us.reference) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
            "AND (CAST(:startDate AS date) IS NULL OR DATE(us.deposit_date) >= CAST(:startDate AS date)) " +
            "AND (CAST(:endDate AS date) IS NULL OR DATE(us.deposit_date) <= CAST(:endDate AS date)) " +
            "AND (:status IS NULL OR us.status = :status) " +
            "ORDER BY us.deposit_date DESC",
            nativeQuery = true)
    List<UserSavings> findPaymentUserSavingsIdFilter(
            @Param("savingsId") String savingsId,
            @Param("reference") String reference,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status

    );




}
