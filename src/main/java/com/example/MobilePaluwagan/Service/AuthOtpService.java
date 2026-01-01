package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Request.OtpRequest;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserVerification;
import com.example.MobilePaluwagan.Repository.UserRepo;
import com.example.MobilePaluwagan.Repository.VerificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthOtpService {
    @Autowired
    VerificationRepo verificationRepo;
    @Autowired
    RegisterService registerService;
    @Autowired
    UserRepo userRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public boolean otpFilter(OtpRequest request){

        UserVerification verification = verificationRepo.findByUserId(request.getUserId());


        if (verification == null){
            return false;
        }

        if(verification.getExpiresAt().isBefore(LocalDateTime.now())){
            return false;
        }

        boolean isValid = encoder.matches(request.getOtp(), verification.getOtpHash());

        if(isValid){
            User user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setActive(true);
            userRepo.save(user);

            verificationRepo.delete(verification);
        }
        return isValid;
    }

    }

