package com.example.MobilePaluwagan.Repository;

import com.example.MobilePaluwagan.Entity.Loan;
import com.example.MobilePaluwagan.Entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLoanRepo extends JpaRepository<Loan, Long> {

    //Optional<Loan> findByUserId(Long userId);

    List<Loan> findAllByUserId(Long userId);
}
