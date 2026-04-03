package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.entity.PaymongoPayment;
import com.example.MobilePaluwagan.entity.Status;
import com.example.MobilePaluwagan.repository.PaymongoPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    private final PaymongoPaymentRepository paymongoPaymentRepository;

    @Scheduled(fixedRate = 60000)
    public void expiredOldPayments() {
        LocalDateTime now = LocalDateTime.now();

        List<PaymongoPayment> expiredPayments = paymongoPaymentRepository.findByStatusAndExpiresAtBefore(Status.PENDING, now);

        for (PaymongoPayment paymongoPayment : expiredPayments) {

            if (paymongoPayment.getStatus() == Status.PAID) {
                continue;
            }
            paymongoPayment.setStatus(Status.EXPIRED);
            paymongoPaymentRepository.save(paymongoPayment);
        }
    }
}
