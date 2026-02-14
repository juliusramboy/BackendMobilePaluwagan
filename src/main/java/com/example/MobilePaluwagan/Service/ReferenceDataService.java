package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Response.EnumDTOResponse;
import com.example.MobilePaluwagan.DTOs.Response.StatusResponse;
import com.example.MobilePaluwagan.Entity.LoanApplication;
import com.example.MobilePaluwagan.Entity.Status;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Repository.LoanApplicationRepo;
import com.example.MobilePaluwagan.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ReferenceDataService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private LoanApplicationRepo loanApplicationRepo;

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


