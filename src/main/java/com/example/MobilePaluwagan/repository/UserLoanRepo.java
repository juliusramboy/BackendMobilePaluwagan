package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserLoanRepo extends JpaRepository<Loan, Long> {

   Optional<Loan> findByUserId(Long userId);

    Loan findAllByUserId(Long userId);

    Loan findByApplicationID(Long applicationID);

    @Query("SELECT SUM(l.totalRepayable) FROM Loan l WHERE l.userId = :userId")
    BigDecimal sumTotalRepayableByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM Loan l WHERE l.endDate < :today " +
            "AND l.loanRepaymentTally < l.totalRepayable")
    List<Loan> findOverdueLoans(@Param("today") LocalDate today);


}
