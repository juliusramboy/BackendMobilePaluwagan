package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.LoanPaymentRepo;
import com.example.MobilePaluwagan.repository.PaymongoPaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayMongoWebhookService {

    @Value("${paymongo.webhook-secret}")
    private String webhookSecret;
    private final PaymongoPaymentRepository paymongoPaymentRepository;
    private final LoanPaymentRepo loanPaymentRepository;
    private final PaymentService paymentService;
    private final SavingsService savingsService;

    public void processWebhook(String payload, String signature) throws Exception {

        if(!isValidSignature(payload, signature)) {
            throw new RuntimeException("Invalid signature — request not from PayMongo!");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> payloadMap = objectMapper.readValue(payload, Map.class);

        Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
        Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
        String eventType = (String) attributes.get("type");

        System.out.println("Event received: " + eventType);


        if("link.payment.paid".equals(eventType)) {
            Map<String, Object> paymentData = (Map<String, Object>) attributes.get("data");
            Map<String, Object> paymentAttributes = (Map<String, Object>) paymentData.get("attributes");

            System.out.println("Payment Data: " + paymentData);
            System.out.println("Payment Attributes: " + paymentAttributes);
            System.out.println("All keys: " + paymentAttributes.keySet());

            String referenceNumber = (String) paymentAttributes.get("reference_number");
            System.out.println("Payment paid! Reference: " + referenceNumber);
            System.out.println("Payment paid! Reference: " + referenceNumber);

            PaymongoPayment payment = paymongoPaymentRepository.findByReferenceNumber(referenceNumber).orElseThrow(()->new RuntimeException("Payment not found!"));

            if (payment.getStatus() == Status.PAID) {
                System.out.println("Payment already processed, skipping...");
                return;
            }

            payment.setStatus(Status.PAID);
            payment.setPaidAt(LocalDateTime.now());
            paymongoPaymentRepository.save(payment);

            List<Map<String, Object>> payments = (List<Map<String, Object>>) paymentAttributes.get("payments");
            Map<String, Object> firstPayment = (Map<String, Object>) payments.get(0);
            Map<String, Object> firstPaymentData = (Map<String, Object>) firstPayment.get("data");
            Map<String, Object> firstPaymentAttributes = (Map<String, Object>) firstPaymentData.get("attributes");
            Map<String, Object> source = (Map<String, Object>) firstPaymentAttributes.get("source");
            String paymentType = (String) source.get("type");

            PaymentMethod paymentMethod;
            switch (paymentType) {
                case "gcash" -> paymentMethod = PaymentMethod.GCASH;
                case "paymaya", "maya" -> paymentMethod = PaymentMethod.MAYA;
                case "card", "credit_card" -> paymentMethod = PaymentMethod.CARD;
                default -> paymentMethod = PaymentMethod.CASH;
            }
            if("LOAN".equals(payment.getPaymentType())) {

                paymentService.processLoanLogic(
                        payment.getReferenceId(),
                        payment.getAmount(),
                        paymentMethod,
                        referenceNumber
                );

                System.out.println("Loan payment saved! ✅");
            } else if ("SAVINGS".equals(payment.getPaymentType())) {

                paymentService.processSavingsOnlinePayment(
                        payment.getReferenceId(),
                        payment.getAmount(),
                        paymentMethod,
                        referenceNumber
                );
            }

        }
    }

    private boolean isValidSignature(String payload, String signature) {
        try{
            String[] parts = signature.split(",");
            String timestamp = parts[0].replace("t=", "");
            String receivedHash = parts[1].replace("te=", "");

            String message = timestamp + "." + payload;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    webhookSecret.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            String computedHash = HexFormat.of().formatHex(mac.doFinal(message.getBytes()));

            return computedHash.equals(receivedHash);

        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
