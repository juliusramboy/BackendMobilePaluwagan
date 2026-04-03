package com.example.MobilePaluwagan.repository;

import com.example.MobilePaluwagan.entity.PaymongoPayment;
import com.example.MobilePaluwagan.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymongoPaymentRepository extends JpaRepository<PaymongoPayment, Long> {

    Optional<PaymongoPayment> findByReferenceNumber(String referenceNumber);
    List<PaymongoPayment> findByStatusAndExpiresAtBefore(Status status, LocalDateTime dateTime);

}
