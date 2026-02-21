package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.DTOs.Response.SavingsPendingPaymentMemberResponse;
import com.example.MobilePaluwagan.Entity.UserBank;
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

    @Query("SELECT new com.example.MobilePaluwagan.DTOs.Response.SavingsPendingPaymentMemberResponse(us.amountDeposit, us.depositDate, us.reference, us.status) " +
    "FROM UserBank ub " +
    "JOIN UserSavings us ON us.userId = ub.userId " +
    "WHERE ub.savingsId = :savingsId " +
    "AND us.status = 'PENDING'")
    List<SavingsPendingPaymentMemberResponse> findPendingPaymentBySavingsId(@Param("savingsId") String savingsId);
}
