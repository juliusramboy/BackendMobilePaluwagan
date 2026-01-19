package com.example.MobilePaluwagan.Controller;


import com.example.MobilePaluwagan.DTOs.Request.ProfileUpdateRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.UserProfileResponse;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileControler {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/info")
    public UserProfileResponse userAllInfo(Authentication authentication){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userid = userDetails.userId();

        UserProfileResponse response = profileService.userAllInfo(userid);

        return response;
    }

    @PatchMapping("/update")
    public ApiResponse<String> userUpdate(Authentication authentication, @RequestBody ProfileUpdateRequest request){
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        Long userId = userPrinciple.userId();

        return profileService.updateProfile(userId, request);
    }

}
