package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Request.OtpVerifyForgotRequest;
import com.example.MobilePaluwagan.dto.Request.RegisterRequest;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class RegisterService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserBankRepo userBankRepo;

    @Autowired
    private VerificationRepo verificationRepo;

    @Autowired
    private UserSavingsRepo userSavingsRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String register(RegisterRequest register) {

        Role defaultRole = roleRepo.findById(2)
                .orElseThrow(() -> new RuntimeException("Default role not found"));


        User user = new User();
        user.setEmail(register.getEmail());
        user.setPassword(encoder.encode(register.getPassword()));
        user.setRole(defaultRole);

        User userdataWithId = userRepo.save(user);


        UserVerification userVerification = new UserVerification();
        userVerification.setUserId(userdataWithId.getId());

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userdataWithId.getId());
        userInfo.setFirstName(register.getFirstName());
        userInfo.setMiddleName(register.getMiddleName());
        userInfo.setLastName(register.getLastName());
        userInfo.setSuffix(register.getSuffix());
        userInfo.setPhoneNumber(register.getPhoneNumber());

        userInfoRepo.save(userInfo);

        UserBank userBank = new UserBank();
        userBank.setUserId(userdataWithId.getId());
        userBank.setAccountBalance(BigDecimal.valueOf(0L));

        userBankRepo.save(userBank);



        String plainOtp = generateOtp();
        String hashedOtp = encoder.encode(plainOtp);

        userVerification.setOtpHash(hashedOtp);
        userVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        userVerification.setCreatedAt(LocalDateTime.now());

        verificationRepo.save(userVerification);

        return plainOtp;
    }

    public String resendOtp(Long userId) {
        UserVerification old = verificationRepo.findByUserId(userId);
        if(old != null) verificationRepo.delete(old);

        return generateAndSaveOtp(userId);
    }


    private String generateAndSaveOtp(Long userId) {
        String plainOtp = generateOtp();
        String hashedOtp = encoder.encode(plainOtp);

        UserVerification userVerification = new UserVerification();
        userVerification.setUserId(userId);
        userVerification.setOtpHash(hashedOtp);
        userVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        userVerification.setCreatedAt(LocalDateTime.now());

        verificationRepo.save(userVerification);

        return plainOtp;
    }

    public String generateOtp(){
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void forgotPassword(OtpVerifyForgotRequest request) {
        // Find the verification record
        UserVerification userVerification = verificationRepo.findByUserId(Long.valueOf(request.getUserId()));

        if (userVerification == null) {
            throw new IllegalArgumentException("User verification not found");
        }

        // Verify OTP using BCrypt matcher
        if (!encoder.matches(request.getOtp(), userVerification.getOtpHash())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Check if OTP is expired (if you have expiration logic)
        if (userVerification.getExpiresAt() != null &&
                userVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        // Find the actual user
        User user = userRepo.findById(Long.valueOf(request.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update the password
        String encodedPassword = encoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        userRepo.save(user);

        // Delete the OTP verification record after successful password change
        verificationRepo.delete(userVerification);

        System.out.println("Password successfully changed for user: " + user.getId());
    }



}