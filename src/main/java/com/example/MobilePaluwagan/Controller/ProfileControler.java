package com.example.MobilePaluwagan.Controller;


import com.example.MobilePaluwagan.DTOs.Request.ProfileUpdateRequest;
import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import com.example.MobilePaluwagan.DTOs.Response.UserProfileResponse;
import com.example.MobilePaluwagan.Entity.UserInfo;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Repository.UserInfoRepo;
import com.example.MobilePaluwagan.Service.ProfileService;
import com.example.MobilePaluwagan.Service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileControler {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private UserInfoRepo userInfoRepo;

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

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<?>> uploadProfile(@RequestParam("file")MultipartFile file, Authentication authentication) throws IOException {
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        String imageUrl = supabaseStorageService.uploadProfileImage(file, userId);

        UserInfo userInfo = userInfoRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userInfo.setProfileImage(imageUrl);
        userInfoRepo.save(userInfo);

        return ResponseEntity.ok(new ApiResponse<>(true, "Upload successful", imageUrl));
    }

    @GetMapping("/image")
    public ResponseEntity<ApiResponse<?>> getProfile(Authentication authentication) {

        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userId = userDetails.userId();

        ApiResponse<?> response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

}
