package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.DTOs.Response.AdminPaymentSavingsSearchResponse;
import com.example.MobilePaluwagan.DTOs.Response.SavingsPendingPaymentMemberResponse;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserBank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBankRepo extends JpaRepository<UserBank, Long> {

    Optional<UserBank> findByAccountBalance(Long accountBalance);

    UserBank findBySavingsId(String savingsId);

//    Optional<UserBank> findByTargetAmount(Long targetAmount);

    Optional<UserBank> findByUserId(Long userId);

    @Query("SELECT new com.example.MobilePaluwagan.DTOs.Response.SavingsPendingPaymentMemberResponse(us.amountDeposit, us.depositDate, us.reference, us.status, ui.profileImage) " +
    "FROM UserBank ub " +
    "JOIN UserSavings us ON us.userId = ub.userId " +
    "JOIN UserInfo ui ON ui.userId = ub.userId " +
    "WHERE ub.savingsId = :savingsId " +
    "AND us.status = 'PENDING'")
    Page<SavingsPendingPaymentMemberResponse> findPendingPaymentBySavingsId(
            @Param("savingsId") String savingsId,
            Pageable pageable);

    @Query("""
    SELECT new com.example.MobilePaluwagan.DTOs.Response.AdminPaymentSavingsSearchResponse(
        u.savingsId,
        u.accountBalance,
        ui.firstName,
        ui.lastName,
        ui.profileImage
    )
    FROM UserBank u
    JOIN UserInfo ui ON ui.userId = u.userId
    WHERE EXISTS (
        SELECT 1 FROM User us
        WHERE us.id = u.userId
        AND us.hasSavingsAccount = true
    )
    AND (
        LOWER(ui.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
        OR LOWER(ui.lastName)  LIKE LOWER(CONCAT('%', :name, '%'))
        OR LOWER(CONCAT(ui.firstName, ' ', ui.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
        OR LOWER(CONCAT(ui.lastName,  ' ', ui.firstName)) LIKE LOWER(CONCAT('%', :name, '%'))
    )
""")
    Page<AdminPaymentSavingsSearchResponse> searchApplicantSavingsByName(@Param("name") String name, Pageable pageable);

    @Query("""
    SELECT new com.example.MobilePaluwagan.DTOs.Response.AdminPaymentSavingsSearchResponse(
        us.savingsId,
            us.accountBalance,
                u.firstName,
                    u.lastName,
                        u.profileImage
    )
        FROM UserBank us
            JOIN UserInfo  u ON u.userId = us.userId
                WHERE EXISTS (
                    SELECT 1 FROM User us
                        WHERE us.id = u.userId
                            AND us.hasSavingsAccount = true
                    )
    """)
    Page<AdminPaymentSavingsSearchResponse> findAllSavingsMembers(Pageable pageable);

}
