package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.Loan;
import com.example.MobilePaluwagan.Entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserLoanRepo extends JpaRepository<Loan, Long> {

   Optional<Loan> findByUserId(Long userId);

    List<Loan> findAllByUserId(Long userId);

    Loan findByApplicationID(Long applicationID);

    @Query("SELECT SUM(l.totalRepayable) FROM Loan l WHERE l.userId = :userId")
    BigDecimal sumTotalRepayableByUserId(@Param("userId") Long userId);


}
