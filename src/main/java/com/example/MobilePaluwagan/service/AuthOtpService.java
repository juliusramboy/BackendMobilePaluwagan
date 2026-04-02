package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Request.OtpRequest;
import com.example.MobilePaluwagan.dto.Response.OtpResponse;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.entity.UserInfo;
import com.example.MobilePaluwagan.entity.UserVerification;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import com.example.MobilePaluwagan.repository.UserRepo;
import com.example.MobilePaluwagan.repository.VerificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    @Autowired
    private UserInfoRepo userInfoRepo;


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
            UserInfo dateVerfied = userInfoRepo.findByUserId(request.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
            dateVerfied.setVerifiedDate(LocalDate.now());
            userInfoRepo.save(dateVerfied);
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

    public OtpResponse sendOtpForgotPassword(String email){

        User isPresent = userRepo.findByEmail(email);

        if(isPresent != null){
            User user = isPresent;

                if(user.isActive()){
                    return new OtpResponse("proceed to change password", user.getId());
                }
            }else {
            return new OtpResponse("Please verify your account or register your account.", null);
            }

        return new OtpResponse("Email not found as a member please register your email", null);
    }

    public OtpResponse sendOtpLogin(String email, String password){

        User isPresent = userRepo.findByEmail(email);

        if(isPresent != null){
            User user = isPresent;

            if (encoder.matches(password, user.getPassword())) {

                if(user.getRole().getRoleName().equals("ROLE_ADMIN") && !user.isActive()){
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

                    return new OtpResponse("We send an email to verify your admin account ", user.getId());
                }

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

                    return new OtpResponse("OTP sent successfully to your email", user.getId());
                }
            }else {
                return new OtpResponse("Email or password do not match", null);
            }
        }
        return new OtpResponse("Please verify your account or register your account.", null);
    }


    }

