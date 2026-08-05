package com.example.MobilePaluwagan.dto.Request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyPinRequest {

    @NotNull(message = "Password is required")
    private String transactionPin;
}
