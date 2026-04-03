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

//    public void processWebhook(String payload, String signature) throws Exception {
//
//        if(!isValidSignature(payload, signature)) {
//            throw new RuntimeException("Invalid signature — request not from PayMongo!");
//        }
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Object> payloadMap = objectMapper.readValue(payload, Map.class);
//
//        Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
//        Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
//        String eventType = (String) attributes.get("type");
//
//        System.out.println("Event received: " + eventType);
//
//
//        if("link.payment.paid".equals(eventType)) {
//            Map<String, Object> paymentData = (Map<String, Object>) attributes.get("data");
//            Map<String, Object> paymentAttributes = (Map<String, Object>) paymentData.get("attributes");
//
//            System.out.println("Payment Data: " + paymentData);
//            System.out.println("Payment Attributes: " + paymentAttributes);
//            System.out.println("All keys: " + paymentAttributes.keySet());
//
//            String referenceNumber = (String) paymentAttributes.get("reference_number");
//            System.out.println("Payment paid! Reference: " + referenceNumber);
//            System.out.println("Payment paid! Reference: " + referenceNumber);
//
//            PaymongoPayment payment = paymongoPaymentRepository.findByReferenceNumber(referenceNumber).orElseThrow(()->new RuntimeException("Payment not found!"));
//
//            if (payment.getStatus() == Status.PAID) {
//                System.out.println("Payment already processed, skipping...");
//                return;
//            }
//
//            payment.setStatus(Status.PAID);
//            payment.setPaidAt(LocalDateTime.now());
//            paymongoPaymentRepository.save(payment);
//
//            List<Map<String, Object>> payments = (List<Map<String, Object>>) paymentAttributes.get("payments");
//            Map<String, Object> firstPayment = (Map<String, Object>) payments.get(0);
//            Map<String, Object> firstPaymentData = (Map<String, Object>) firstPayment.get("data");
//            Map<String, Object> firstPaymentAttributes = (Map<String, Object>) firstPaymentData.get("attributes");
//            Map<String, Object> source = (Map<String, Object>) firstPaymentAttributes.get("source");
//            String paymentType = (String) source.get("type");
//
//            PaymentMethod paymentMethod;
//            switch (paymentType) {
//                case "gcash" -> paymentMethod = PaymentMethod.GCASH;
//                case "paymaya", "maya" -> paymentMethod = PaymentMethod.MAYA;
//                case "card", "credit_card" -> paymentMethod = PaymentMethod.CARD;
//                default -> paymentMethod = PaymentMethod.CASH;
//            }
//            if("LOAN".equals(payment.getPaymentType())) {
//
//                paymentService.processLoanLogic(
//                        payment.getReferenceId(),
//                        payment.getAmount(),
//                        paymentMethod,
//                        referenceNumber
//                );
//
//                System.out.println("Loan payment saved! ✅");
//            } else if ("SAVINGS".equals(payment.getPaymentType())) {
//
//                paymentService.processSavingsOnlinePayment(
//                        payment.getReferenceId(),
//                        payment.getAmount(),
//                        paymentMethod,
//                        referenceNumber
//                );
//            }
//
//        }
//    }

    public void processWebhook(String payload, String signature) throws Exception {
        if (!isValidSignature(payload, signature)) {
            throw new RuntimeException("Invalid signature — request not from PayMongo!");
        }
        processPayload(payload);
    }


    private void processPayload(String payload) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> payloadMap = objectMapper.readValue(payload, Map.class);

        Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
        Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
        String eventType = (String) attributes.get("type");

        System.out.println("Event received: " + eventType);

        switch (eventType) {
            case "link.payment.paid"         -> handleLinkPaymentPaid(attributes);
            case "payment_intent.succeeded"  -> handleIntentSucceeded(attributes);
            case "payment.paid" ->  handlePaymentPaid(attributes);
            case "qrph.expired" -> handleQrphExpired(attributes);
            case "payment.failed"        -> handlePaymentFailed(attributes);
            default -> System.out.println("Unhandled event type: " + eventType);
        }
    }

    // ─── link.payment.expired ───────────────────────────────────────────────────

    private void handleQrphExpired(Map<String, Object> attributes) {
        try {
            // Get the payment intent ID from attributes
            String intentId = (String) attributes.get("payment_intent_id");

            PaymongoPayment payment = paymongoPaymentRepository
                    .findByReferenceNumber(intentId)
                    .orElse(null);

            if (payment != null && payment.getStatus() == Status.PENDING) {
                payment.setStatus(Status.EXPIRED);
                paymongoPaymentRepository.save(payment);
                System.out.println("QR expired for payment: " + intentId);
            }
        } catch (Exception e) {
            System.out.println("Error handling qrph.expired: " + e.getMessage());
        }
    }

    // ─── link.payment.failed ───────────────────────────────────────────────────

    private void handlePaymentFailed(Map<String, Object> attributes) {
        try {
            String intentId = (String) attributes.get("payment_intent_id");

            PaymongoPayment payment = paymongoPaymentRepository
                    .findByReferenceNumber(intentId)
                    .orElse(null);

            if (payment != null && payment.getStatus() == Status.PENDING) {
                payment.setStatus(Status.FAILED);
                paymongoPaymentRepository.save(payment);
                System.out.println("Payment failed for: " + intentId);
            }
        } catch (Exception e) {
            System.out.println("Error handling payment.failed: " + e.getMessage());
        }
    }

    // ─── link.payment.paid ───────────────────────────────────────────────────

    private void handleLinkPaymentPaid(Map<String, Object> attributes) {
        Map<String, Object> paymentData = (Map<String, Object>) attributes.get("data");
        Map<String, Object> paymentAttributes = (Map<String, Object>) paymentData.get("attributes");

        String referenceNumber = (String) paymentAttributes.get("reference_number");
        System.out.println("link.payment.paid — reference: " + referenceNumber);

        PaymongoPayment payment = paymongoPaymentRepository
                .findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + referenceNumber));

        if (payment.getStatus() == Status.PAID) {
            System.out.println("Already processed, skipping.");
            return;
        }

        // Extract payment method from nested payments list
        List<Map<String, Object>> payments = (List<Map<String, Object>>) paymentAttributes.get("payments");
        PaymentMethod paymentMethod = extractPaymentMethod(payments);

        handlePaymentCompletion(payment, paymentMethod, referenceNumber);
    }

    // ─── payment_intent.succeeded ────────────────────────────────────────────

    private void handleIntentSucceeded(Map<String, Object> attributes) {
        Map<String, Object> paymentData = (Map<String, Object>) attributes.get("data");
        String intentId = (String) paymentData.get("id");

        System.out.println("payment_intent.succeeded — intentId: " + intentId);

        // intentId was saved as referenceNumber during createPaymentIntent
        PaymongoPayment payment = paymongoPaymentRepository
                .findByReferenceNumber(intentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for intent: " + intentId));

        if (payment.getStatus() == Status.PAID) {
            System.out.println("Already processed, skipping.");
            return;
        }

        // Extract payment method
        Map<String, Object> paymentAttributes = (Map<String, Object>) paymentData.get("attributes");
        List<Map<String, Object>> payments = (List<Map<String, Object>>) paymentAttributes.get("payments");
        PaymentMethod paymentMethod = extractPaymentMethod(payments);

        handlePaymentCompletion(payment, paymentMethod, intentId);
    }

    // ─── handle payment paid ─────────────────────────────────────────────────────────

    private void handlePaymentPaid(Map<String, Object> attributes) {
        Map<String, Object> paymentData = (Map<String, Object>) attributes.get("data");
        Map<String, Object> paymentAttributes = (Map<String, Object>) paymentData.get("attributes");

        // PayMongo includes the intent ID in the payment object
        String intentId = (String) paymentAttributes.get("payment_intent_id");

        System.out.println("payment.paid — intentId: " + intentId);

        PaymongoPayment payment = paymongoPaymentRepository
                .findByReferenceNumber(intentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for intent: " + intentId));

        if (payment.getStatus() == Status.EXPIRED){
            return;
        }

        if (payment.getStatus() == Status.PAID) {
            System.out.println("Already processed, skipping.");
            return;
        }

        // Determine the source type (gcash, qr_code, etc.)
        Map<String, Object> source = (Map<String, Object>) paymentAttributes.get("source");
        String type = (String) source.get("type");

        // Map it to your Enum
        PaymentMethod paymentMethod = switch (type) {
            case "gcash" -> PaymentMethod.GCASH;
            case "paymaya", "maya" -> PaymentMethod.MAYA;
            case "qrph" -> PaymentMethod.QRPH;
            default -> PaymentMethod.CASH;
        };

        handlePaymentCompletion(payment, paymentMethod, intentId);
    }

    // ─── Shared logic ─────────────────────────────────────────────────────────

    private void handlePaymentCompletion(PaymongoPayment payment,
                                         PaymentMethod paymentMethod,
                                         String bankReference) {
        payment.setStatus(Status.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymongoPaymentRepository.save(payment);

        if ("LOAN".equals(payment.getPaymentType())) {
            paymentService.processLoanLogic(
                    payment.getReferenceId(),
                    payment.getAmount(),
                    paymentMethod,
                    bankReference
            );
            System.out.println("Loan payment processed. ✅");

        } else if ("SAVINGS".equals(payment.getPaymentType())) {
            paymentService.processSavingsOnlinePayment(
                    payment.getReferenceId(),
                    payment.getAmount(),
                    paymentMethod,
                    bankReference
            );
            System.out.println("Savings payment processed. ✅");
        }
    }

    private PaymentMethod extractPaymentMethod(List<Map<String, Object>> payments) {
        if (payments == null || payments.isEmpty()) return PaymentMethod.CASH;

        try {
            Map<String, Object> firstPayment = (Map<String, Object>) payments.get(0);
            Map<String, Object> firstData = (Map<String, Object>) firstPayment.get("data");
            Map<String, Object> firstAttrs = (Map<String, Object>) firstData.get("attributes");
            Map<String, Object> source = (Map<String, Object>) firstAttrs.get("source");
            String type = (String) source.get("type");

            return switch (type) {
                case "gcash"                -> PaymentMethod.GCASH;
                case "paymaya", "maya" , "qrph"     -> PaymentMethod.MAYA;
                case "card", "credit_card"  -> PaymentMethod.CARD;
                default                     -> PaymentMethod.CASH;
            };
        } catch (Exception e) {
            System.out.println("Could not extract payment method, defaulting to CASH: " + e.getMessage());
            return PaymentMethod.CASH;
        }
    }


    // this is for testing
    public void processWebhookWithoutSignature(String payload) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> payloadMap = objectMapper.readValue(payload, Map.class);

        Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
        Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
        String eventType = (String) attributes.get("type");

        System.out.println("Simulated Event received: " + eventType);

        if("link.payment.paid".equals(eventType)) {
            Map<String, Object> paymentData = (Map<String, Object>) attributes.get("data");
            Map<String, Object> paymentAttributes = (Map<String, Object>) paymentData.get("attributes");

            String referenceNumber = (String) paymentAttributes.get("reference_number");
            System.out.println("Simulated Payment paid! Reference: " + referenceNumber);

            PaymongoPayment payment = paymongoPaymentRepository.findByReferenceNumber(referenceNumber)
                    .orElseThrow(() -> new RuntimeException("Payment not found!"));

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
                case "paymaya", "maya", "qrph" -> paymentMethod = PaymentMethod.MAYA;
                case "card", "credit_card" -> paymentMethod = PaymentMethod.CARD;
                default -> paymentMethod = PaymentMethod.CASH;
            }

            if ("LOAN".equals(payment.getPaymentType())) {
                paymentService.processLoanLogic(
                        payment.getReferenceId(),
                        payment.getAmount(),
                        paymentMethod,
                        referenceNumber
                );
                System.out.println("Simulated Loan payment saved! ✅");
            } else if ("SAVINGS".equals(payment.getPaymentType())) {
                paymentService.processSavingsOnlinePayment(
                        payment.getReferenceId(),
                        payment.getAmount(),
                        paymentMethod,
                        referenceNumber
                );
                System.out.println("Simulated Savings payment saved! ✅");
            }
        }
    }

    private boolean isValidSignature(String payload, String signature) {
        try{
            String timestamp = "";
            String receivedHash = "";

            String[] parts = signature.split(",");

            for (String part : parts) {
                if (part.startsWith("t=")) timestamp = part.substring(2);
                if (part.startsWith("v1=")) receivedHash = part.substring(3); // live mode
                if (part.startsWith("te=")) receivedHash = part.substring(3); // test mode
            }

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
