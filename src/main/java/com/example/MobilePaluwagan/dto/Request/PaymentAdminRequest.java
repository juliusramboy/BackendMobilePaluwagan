package com.example.MobilePaluwagan.dto.Request;


import com.example.MobilePaluwagan.entity.PaymentMethod;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAdminRequest {

    private String genId;
    private String applicationId;
    private Double amount;
    private String reference;
    private PaymentMethod paymentMethod;
    @Nullable
    private String bankReference;
}
