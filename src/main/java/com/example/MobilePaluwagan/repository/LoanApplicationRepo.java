package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.dto.Response.ApplicantsFullInfoAdmin;
import com.example.MobilePaluwagan.dto.Response.LoanApplicantsAdmin;
import com.example.MobilePaluwagan.entity.LoanApplication;
import com.example.MobilePaluwagan.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepo extends JpaRepository<LoanApplication, Long> {

    Optional<LoanApplication> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    Long countByStatus(Status status);

    List<LoanApplication> findAllByUserId(Long userId);

    boolean existsByUserIdAndStatusIn(Long userId, List<Status> statuses);

    Optional<LoanApplication> findByApplicationID(Long applicationID);
    boolean existsByApplicationID(Long applicationID);

    boolean existsByUserIdAndStatus(Long userId, Status status);

    Optional<LoanApplication> findTopByUserIdAndStatusInOrderByApplicationIDDesc(Long userId, List<Status> statuses);

    @Query("SELECT new com.example.MobilePaluwagan.dto.Response.LoanApplicantsAdmin(" +
            "la.userId, la.applicationID, la.totalRepayable, la.weeklyPay, la.requestedAmount, la.interestRate, la.interest, la.repayPeriodDays, la.repayPeriodWeeks, la.startDate, la.endDate, la.status, lu.firstName, lu.lastName, lu.profileImage) " +
            "FROM LoanApplication la " +
            "JOIN la.userInfo lu " +
            "WHERE la.status = :status")
    Page<LoanApplicantsAdmin> findLoanByStatus(@Param("status") Status status, Pageable pageable);



    @Query("SELECT new com.example.MobilePaluwagan.dto.Response.ApplicantsFullInfoAdmin(" +
            "la.applicationID, la.requestedAmount, la.interestRate, la.interest, " +
            "la.weeklyPay, la.totalRepayable, la.repayPeriodDays, la.repayPeriodWeeks, " +
            "la.startDate, la.endDate, la.userInfo.firstName, la.userInfo.lastName, la.userInfo.profileImage) " +
            "FROM LoanApplication la " +
            "WHERE la.applicationID = :applicationID")
    ApplicantsFullInfoAdmin findLoanApplicantsFullInfo(@Param("applicationID") Long applicationID);





}
