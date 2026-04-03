package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Request.PaymongoIntentRequest;
import com.example.MobilePaluwagan.dto.Request.PaymongoRequest;
import com.example.MobilePaluwagan.entity.PaymongoPayment;
import com.example.MobilePaluwagan.entity.Status;
import com.example.MobilePaluwagan.repository.PaymongoPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayMongoService {

    @Value("${paymongo.secret-key}")
    private String secretKey;
    private final WebClient webClient;
    private final PaymongoPaymentRepository paymongoPaymentRepository;

    public String createPaymentLink(Long userId, PaymongoRequest request) {

        // ✅ Fix 1 — add colon at the end before encoding
        String credentials = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        long amountInCentavos = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        // ✅ Fix 2 — wrap attributes inside "data"
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("amount", amountInCentavos);
        attributes.put("description", request.getDescription());
        attributes.put("currency", "PHP");

        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);  // ← was missing this wrapper!

        Map<String, Object> response = webClient.post()
                .uri("/links")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.out.println("PayMongo Error: " + errorBody);
                                    return Mono.error(new RuntimeException(errorBody));
                                })
                )
                .bodyToMono(Map.class)
                .block();

        // ✅ Fix 3 — correct way to extract checkout_url
        Map<String, Object> responseData = (Map<String, Object>) response.get("data");
        Map<String, Object> responseAttributes = (Map<String, Object>) responseData.get("attributes");

        String checkUrl = (String) responseAttributes.get("checkout_url");
        String paymongoLinkId = (String) responseData.get("id");
        String referenceNumber = (String) responseAttributes.get("reference_number");

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        PaymongoPayment payment = new PaymongoPayment();
        payment.setUserId(userId);
        payment.setPaymentType(request.getPaymentType());
        payment.setReferenceId(request.getReferenceId());
        payment.setPaymongoLinkId(paymongoLinkId);
        payment.setReferenceNumber(referenceNumber);
        payment.setCheckoutUrl(checkUrl);
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setExpiresAt(expiresAt);
        payment.setStatus(Status.PENDING);

        paymongoPaymentRepository.save(payment);
        System.out.println("Paymongo Payment Created Successfully: " + referenceNumber);

        return checkUrl;
    }

    // test
    public Map<String, Object> createPaymentIntent(Long userId, PaymongoIntentRequest request) {

        String credentials = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        long amountInCentavos = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("amount", amountInCentavos);
        attributes.put("currency", "PHP");
        attributes.put("description", request.getDescription());
        attributes.put("payment_method_allowed", List.of(request.getMethodType()));

        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);

        Map<String, Object> response = webClient.post()
                .uri("/payment_intents")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(errorBody)))
                )
                .bodyToMono(Map.class)
                .block();

        Map<String, Object> responseData = (Map<String, Object>) response.get("data");
        Map<String, Object> responseAttributes = (Map<String, Object>) responseData.get("attributes");

        String intentId = (String) responseData.get("id");
        String clientKey = (String) responseAttributes.get("client_key");

        // Save to DB as PENDING
        PaymongoPayment payment = new PaymongoPayment();
        payment.setUserId(userId);
        payment.setPaymentType(request.getPaymentType());
        payment.setReferenceId(request.getReferenceId());
        payment.setPaymongoLinkId(intentId);
        payment.setReferenceNumber(intentId);
        payment.setAmount(request.getAmount());
        payment.setDescription(request.getDescription());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(Status.PENDING);
        paymongoPaymentRepository.save(payment);

        return Map.of(
                "intentId", intentId,
                "clientKey", clientKey,
                "amount", request.getAmount()
        );
    }


    public Map<String, Object> attachPaymentMethod(String intentId, String methodType) {
        try {
            String credentials = Base64.getEncoder()
                    .encodeToString((secretKey + ":").getBytes());

            // Step 1 — Create payment method
            Map<String, Object> methodAttributes = new HashMap<>();
            methodAttributes.put("type", methodType);

            Map<String, Object> methodData = new HashMap<>();
            methodData.put("attributes", methodAttributes);

            Map<String, Object> methodBody = new HashMap<>();
            methodBody.put("data", methodData);

            Map<String, Object> methodResponse = webClient.post()
                    .uri("/payment_methods")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .bodyValue(methodBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        System.out.println("PayMongo Error (payment_methods): " + errorBody);
                                        return Mono.error(new RuntimeException(errorBody));
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> methodResponseData = (Map<String, Object>) methodResponse.get("data");
            String paymentMethodId = (String) methodResponseData.get("id");
            System.out.println("Payment Method ID: " + paymentMethodId);

            // Step 2 — Attach to intent
            Map<String, Object> attachAttributes = new HashMap<>();
            attachAttributes.put("payment_method", paymentMethodId);
            attachAttributes.put("return_url", "http://localhost:5173/payment/success");

            Map<String, Object> attachData = new HashMap<>();
            attachData.put("attributes", attachAttributes);

            Map<String, Object> attachBody = new HashMap<>();
            attachBody.put("data", attachData);

            Map<String, Object> attachResponse = webClient.post()
                    .uri("/payment_intents/" + intentId + "/attach")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .bodyValue(attachBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        System.out.println("PayMongo Error (attach): " + errorBody);
                                        return Mono.error(new RuntimeException(errorBody));
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> attachResponseData = (Map<String, Object>) attachResponse.get("data");
            Map<String, Object> attachResponseAttributes = (Map<String, Object>) attachResponseData.get("attributes");
            String status = (String) attachResponseAttributes.get("status");

            // 🆕 Debug logs
            System.out.println("Status: " + status);
            System.out.println("Next action: " + attachResponseAttributes.get("next_action"));

            if ("awaiting_next_action".equals(status)) {
                Map<String, Object> nextAction = (Map<String, Object>) attachResponseAttributes.get("next_action");
                System.out.println("Next action keys: " + nextAction.keySet());

                // ✅ QRPh — key is "code"
                if (nextAction.containsKey("code")) {
                    Map<String, Object> code = (Map<String, Object>) nextAction.get("code");
                    System.out.println("Code keys: " + code.keySet());
                    String imageUrl = (String) code.get("image_url");
                    String testUrl = (String) code.get("test_url");
                    return Map.of(
                            "status", status,
                            "qrCodeImage", imageUrl,
                            "testUrl", testUrl
                    );
                }

                // For GCash/Maya — key is "redirect"
                if (nextAction.containsKey("redirect")) {
                    Map<String, Object> redirect = (Map<String, Object>) nextAction.get("redirect");
                    String redirectUrl = (String) redirect.get("url");
                    return Map.of(
                            "status", status,
                            "redirectUrl", redirectUrl
                    );
                }
            }

            return Map.of("status", status);

        } catch (Exception e) {
            System.out.println("ERROR in attachPaymentMethod: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
