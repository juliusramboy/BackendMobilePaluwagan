package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Request.RegisterRequest;
import com.example.MobilePaluwagan.DTOs.Request.VerificationRequest;
import com.example.MobilePaluwagan.Entity.*;
import com.example.MobilePaluwagan.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String register(RegisterRequest register) {

        Role defaultRole = roleRepo.findById(2)
                .orElseThrow(() -> new RuntimeException("Default role not found"));


        User user = new User();
        user.setEmail(register.getEmail());
        user.setPassword(encoder.encode(register.getPassword()));
        user.setRole(defaultRole);

        User userdataWithId = userRepo.save(user);

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
        userBank.setTargetAmount(register.getTargetAmount());
        userBank.setAccountBalance(0L);

        userBankRepo.save(userBank);

        UserVerification userVerification = new UserVerification();
        userVerification.setUserId(userdataWithId.getId());

        String plainOtp = generateOtp();
        String hashedOtp = encoder.encode(plainOtp);

        userVerification.setOtpHash(hashedOtp);
        userVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        userVerification.setCreatedAt(LocalDateTime.now());

        verificationRepo.save(userVerification);

        return plainOtp;
    }
    public void resendOtp(VerificationRequest otp){

        UserVerification userVerification = new UserVerification();
        userVerification.setUserId(otp.getUserId());
        userVerification.setOtpHash(otp.getOtpHash());
        userVerification.setExpiresAt(otp.getExpiresAt());

        verificationRepo.save(userVerification);
    }

    public String generateOtp(){
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

}