package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Request.OtpRequest;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserVerification;
import com.example.MobilePaluwagan.Repository.UserRepo;
import com.example.MobilePaluwagan.Repository.VerificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthOtpService {
    @Autowired
    VerificationRepo verificationRepo;
    @Autowired
    RegisterService registerService;
    @Autowired
    UserRepo userRepo;
    @Autowired
    private EmailService emailService;


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

    public boolean resendEmail(Long userId){

        Optional<User> isPresent = userRepo.findById(userId);

        if(isPresent.isPresent()){
            User user = isPresent.get();

            if(!user.isActive()){
                UserVerification verification = verificationRepo.findByUserId(userId);

                if(verification != null){
                    verificationRepo.delete(verification);
                }

                String plainNewOtp = registerService.generateOtp();
                String hashedNewOtp = encoder.encode(plainNewOtp);


                UserVerification userVerification = new UserVerification();
                userVerification.setUserId(userId);
                userVerification.setOtpHash(hashedNewOtp);
                userVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
                userVerification.setCreatedAt(LocalDateTime.now());

                verificationRepo.save(userVerification);

                emailService.resendVerificationEmail(user.getEmail(), plainNewOtp);

                return true;
            }
        }
        return false;
    }

    public boolean sendOtpLogin(String email){

        User isPresent = userRepo.findByEmail(email);

        if(isPresent != null){
            User user = isPresent;

            if(user.isActive()){
                UserVerification verification = verificationRepo.findByUserId(user.getId());

                if(verification != null){
                    verificationRepo.delete(verification);
                }

                String plainNewOtp = registerService.generateOtp();
                String hashedNewOtp = encoder.encode(plainNewOtp);


                UserVerification userVerification = new UserVerification();
                userVerification.setUserId(user.getId());
                userVerification.setOtpHash(hashedNewOtp);
                userVerification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
                userVerification.setCreatedAt(LocalDateTime.now());

                verificationRepo.save(userVerification);

                emailService.sendOtpInLogin(user.getEmail(), plainNewOtp);

                return true;
            }
        }
        return false;
    }

    }

