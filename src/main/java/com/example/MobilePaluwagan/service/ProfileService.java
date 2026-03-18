package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.controller.SseController;
import com.example.MobilePaluwagan.dto.Request.ProfileUpdateRequest;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.UserProfileResponse;
import com.example.MobilePaluwagan.entity.LoanPayment;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.entity.UserInfo;
import com.example.MobilePaluwagan.repository.LoanPaymentRepo;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import com.example.MobilePaluwagan.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserInfoRepo userInfoRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final SseController sseController;
    private final LoanPaymentRepo loanPaymentRepo;


    public UserProfileResponse userAllInfo(Long userId){
        UserInfo userInfo = userInfoRepo.findByUserId(userId).orElseThrow(() -> new RuntimeException("User info not found in user info"));
        User user = userRepo.findById(userId).orElseThrow(()-> new RuntimeException("user not found in users"));

        return new UserProfileResponse(
                userInfo.getFirstName(),
                userInfo.getLastName(),
                userInfo.getMiddleName(),
                userInfo.getSuffix(),
                userInfo.getPhoneNumber(),
                userInfo.getVerifiedDate(),
                userInfo.getAddress(),
                userInfo.getBirthDay(),
                userInfo.getGender(),
                userInfo.getProfileImage(),
                user.getEmail()

        );
    }

    @Transactional
    public ApiResponse<String> updateProfile(Long userId, ProfileUpdateRequest request) {


        UserInfo info = userInfoRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));



        if (request.getFirstName() != null) {
            info.setFirstName(request.getFirstName());
        }

        if (request.getMiddleName() != null) {
            info.setMiddleName(request.getMiddleName());
        }

        if (request.getLastName() != null) {
            info.setLastName(request.getLastName());
        }

        if (request.getSuffix() != null) {
            info.setSuffix(request.getSuffix());
        }

        if (request.getGender() != null) {
            info.setGender(request.getGender());
        }

        if (request.getAddress() != null) {
            info.setAddress(request.getAddress());
        }

        if (request.getBirthDay() != null) {
            info.setBirthDay(request.getBirthDay());
        }

        if (request.getPhoneNumber() != null) {
            info.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getEmail() != null){
            user.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && request.getOldPassword() != null){

            if (passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
                    String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

                    user.setPassword(encodedNewPassword);

                    userRepo.save(user);
                } else {
                    return new ApiResponse<>(
                            false,
                            "New password cannot be null or blank",
                            null
                    );
                }
            } else {
                return new ApiResponse<>(
                        false,
                        "Old password does not match",
                        null
                );
            }

        }

        UserInfo savedUser = userInfoRepo.save(info);

        sseController.notifyUpdate();
        return new ApiResponse<>(
                true,
                "Successfully updated profile",
                "Profile updated"
        );
    }

    public Page<LoanPayment> getAllLoanPayments(Long userId, int page, int size) {
        return loanPaymentRepo.findAllByUserId(userId, PageRequest.of(page, size));
    }



}
