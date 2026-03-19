package com.example.MobilePaluwagan.service;

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
        payment.setStatus(Status.PENDING);

        paymongoPaymentRepository.save(payment);
        System.out.println("Paymongo Payment Created Successfully: " + referenceNumber);

        return checkUrl;
    }
}
