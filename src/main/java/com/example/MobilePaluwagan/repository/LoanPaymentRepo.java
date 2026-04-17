package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.dto.Response.AdminPaymentLoanSearchResponse;
import com.example.MobilePaluwagan.dto.Response.UserListPayments;
import com.example.MobilePaluwagan.entity.LoanPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanPaymentRepo extends JpaRepository<LoanPayment, Long> {
    Optional<LoanPayment> findByUserId(Long userId);
    List<LoanPayment> findByLoanId(Long loanId);
    @Query("""
        SELECT new com.example.MobilePaluwagan.dto.Response.UserListPayments( 
            p.amountPaid,
            p.paymentDate
            
        )
        FROM LoanPayment p
        WHERE p.loanId = :loanId
        """)
    List<UserListPayments> findUserListPaymentsByLoanId(@Param("loanId") Long loanId);

    Page<LoanPayment> findAllByUserId(Long userId, Pageable pageable);
    List<LoanPayment> findAllById(Long loanId);

    void deleteByUserId(Long userId);

    @Query("SELECT SUM(lp.amountPaid) FROM LoanPayment lp WHERE lp.loanId  = :loanId AND lp.status = 'PAID'")
    BigDecimal sumAllPaidByLoanId(@Param("loanId") Long loanId);

    Optional<LoanPayment> findTopByLoanIdOrderByPaymentDateDesc(Long loanId);


    @Query("SELECT e FROM LoanPayment e ORDER BY e.id desc LIMIT 1")
    Optional<LoanPayment> findLastRef();

    @Query(value = "SELECT lp.* FROM loan_payment lp " +
            "JOIN loan l ON lp.loan_id = l.id " +
            "WHERE l.user_id = :userId",
            nativeQuery = true)
    List<LoanPayment> findPaymentsByUserIdNative(@Param("userId") Long userId);


    @Query(value = "SELECT lp.* FROM loan_payment lp " +
            "JOIN loan l ON lp.loan_id = l.id " +
            "WHERE l.user_id = :userId " +
            "AND (:reference IS NULL OR LOWER(lp.reference_number) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
            "AND (:startDate IS NULL OR lp.payment_date >= :startDate) " +
            "AND (:endDate IS NULL OR lp.payment_date <= :endDate) " +
            "AND (:status IS NULL OR lp.status = :status) " +
            "AND (:paymentMethod IS NULL OR lp.payment_method = :paymentMethod) " +
            "ORDER BY lp.payment_date DESC",
            countQuery = "SELECT COUNT(*) FROM loan_payment lp " +  // ← add this
                    "JOIN loan l ON lp.loan_id = l.id " +
                    "WHERE l.user_id = :userId " +
                    "AND (:reference IS NULL OR LOWER(lp.reference_number) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
                    "AND (:startDate IS NULL OR lp.payment_date >= :startDate) " +
                    "AND (:endDate IS NULL OR lp.payment_date <= :endDate) " +
                    "AND (:status IS NULL OR lp.status = :status) " +
                    "AND (:paymentMethod IS NULL OR lp.payment_method = :paymentMethod)",
            nativeQuery = true)
    Page<LoanPayment> findPaymentsByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("reference") String reference,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status,
            @Param("paymentMethod") String paymentMethod,
            Pageable pageable
    );

    @Query("""
    SELECT new com.example.MobilePaluwagan.dto.Response.AdminPaymentLoanSearchResponse(
        l.applicationID,
        l.weeklyPay,
        u.firstName,
        u.lastName,
        u.profileImage,
        l.totalRepayable ,
        l.loanRepaymentTally
    )
    FROM Loan l
    JOIN UserInfo u ON u.userId = l.userId
    WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
    OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
    OR LOWER(CONCAT(u.lastName,  ' ', u.firstName)) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    Page<AdminPaymentLoanSearchResponse> searchApplicantLoanByName(@Param("name") String name, Pageable pageable);

    @Query("""
    SELECT new com.example.MobilePaluwagan.dto.Response.AdminPaymentLoanSearchResponse(
        l.applicationID,
        l.weeklyPay,
        u.firstName,
        u.lastName,
        u.profileImage,
        l.totalRepayable,
        l.loanRepaymentTally
    )
    FROM Loan l
    JOIN UserInfo u ON u.userId = l.userId
""")
    Page<AdminPaymentLoanSearchResponse> findAllLoanApplicants(Pageable pageable);






}
