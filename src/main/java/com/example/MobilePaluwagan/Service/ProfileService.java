package com.example.MobilePaluwagan.Service;

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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private UserInfoRepo userInfoRepo;
    @Autowired
    private UserRepo userRepo;

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
                user.getEmail()
        );
    }

    @Transactional
    public ApiResponse<String> updateProfile(Long userId, ProfileUpdateRequest request) {


        UserInfo user = userInfoRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId
                ));



        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getMiddleName() != null) {
            user.setMiddleName(request.getMiddleName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getSuffix() != null) {
            user.setSuffix(request.getSuffix());
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        if (request.getBirthDay() != null) {
            user.setBirthDay(request.getBirthDay());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }


        UserInfo savedUser = userInfoRepo.save(user);

        return new ApiResponse<>(
                true,
                "Successfully updated profile",
                "Profile updated"
        );
    }
}
