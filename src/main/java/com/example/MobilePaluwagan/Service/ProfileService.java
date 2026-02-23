package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Controller.LoanSseController;
import com.example.MobilePaluwagan.DTOs.Request.ProfileUpdateRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.UserProfileResponse;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserInfo;
import com.example.MobilePaluwagan.Repository.UserInfoRepo;
import com.example.MobilePaluwagan.Repository.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private UserInfoRepo userInfoRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LoanSseController loanSseController;


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

        loanSseController.notifyLoan();
        return new ApiResponse<>(
                true,
                "Successfully updated profile",
                "Profile updated"
        );
    }


}
