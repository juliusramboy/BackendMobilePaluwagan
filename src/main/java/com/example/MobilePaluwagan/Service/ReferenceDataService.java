package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Response.EnumDTOResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ReferenceDataService {

    public List<EnumDTOResponse> getPaymentMethodsUserAndAdmin() {
        return Arrays.asList(
                new EnumDTOResponse("GCASH"),
                new EnumDTOResponse("CASH"),
                new EnumDTOResponse("MAYA")
        );
    }

    public List<EnumDTOResponse> getFilterStatusUser() {
        return Arrays.asList(
                new EnumDTOResponse("PAID"),
                new EnumDTOResponse("APPROVED"),
                new EnumDTOResponse("REJECTED"),
                new EnumDTOResponse("PAST DUE")
        );
    }

    public List<EnumDTOResponse> getFilterStatusAdmin() {
        return Arrays.asList(
                new EnumDTOResponse("PAID"),
                new EnumDTOResponse("APPROVED"),
                new EnumDTOResponse("REJECTED"),
                new EnumDTOResponse("PAST DUE"),
                new EnumDTOResponse("ACTIVE"),
                new EnumDTOResponse("SUCCESS"),
                new EnumDTOResponse("FAILED")

        );
    }
}


