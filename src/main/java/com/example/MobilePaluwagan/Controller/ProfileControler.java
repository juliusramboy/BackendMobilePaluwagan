package com.example.MobilePaluwagan.Controller;


import com.example.MobilePaluwagan.DTOs.Response.UserProfileResponse;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileControler {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/profile/info")
    public UserProfileResponse userAllInfo(Authentication authentication){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userid = userDetails.userId();

        UserProfileResponse response = profileService.userAllInfo(userid);

        return response;
    }
}
