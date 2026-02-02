package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.DTOs.Response.ApplicantsFullInfoAdmin;
import com.example.MobilePaluwagan.DTOs.Response.LoanApplicantsAdmin;
import com.example.MobilePaluwagan.Entity.LoanApplication;
import com.example.MobilePaluwagan.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepo extends JpaRepository<LoanApplication, Long> {

    Optional<LoanApplication> findByUserId(Long userId);

    List<LoanApplication> findAllByUserId(Long userId);

    boolean existsByUserIdAndStatusIn(Long userId, List<Status> statuses);

    @Query("SELECT new com.example.MobilePaluwagan.DTOs.Response.LoanApplicantsAdmin(" +
            "la.userId, la.applicationID, la.totalRepayable, la.weeklyPay, lu.firstName, lu.lastName) " +
            "FROM LoanApplication la " +
            "JOIN la.userInfo lu " +
            "WHERE la.status = :status")
    List<LoanApplicantsAdmin> findLoanByStatus(@Param("status") Status status);

//    @Query("SELECT new com.example.MobilePaluwagan.DTOs.Response.ApplicantsFullInfoAdmin(" +
//    "la.applicationID, la.requestedAmount, la.interestRate, la.interest, la.weeklyPay, la.totalRepayable, la.repayPeriodDays, la.repayPeriodWeeks, la.startDate, la.endDate, lu.firstName, lu.lastName) " +
//            "FROM LoanApplication la " +
//            "JOIN UserInfo lu ON la.userId = lu.userId " +
//            "WHERE la.applicationID = :applicationID")
//    ApplicantsFullInfoAdmin findLoanApplicantsFullInfo(@Param("applicationID") Long applicationID);

    @Query("SELECT new com.example.MobilePaluwagan.DTOs.Response.ApplicantsFullInfoAdmin(" +
            "la.applicationID, la.requestedAmount, la.interestRate, la.interest, " +
            "la.weeklyPay, la.totalRepayable, la.repayPeriodDays, la.repayPeriodWeeks, " +
            "la.startDate, la.endDate, la.userInfo.firstName, la.userInfo.lastName) " +
            "FROM LoanApplication la " +
            "WHERE la.applicationID = :applicationID")
    ApplicantsFullInfoAdmin findLoanApplicantsFullInfo(@Param("applicationID") Long applicationID);





}
