package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Response.UserProfileResponse;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserInfo;
import com.example.MobilePaluwagan.Repository.UserInfoRepo;
import com.example.MobilePaluwagan.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                user.getEmail()
        );
    }
}
