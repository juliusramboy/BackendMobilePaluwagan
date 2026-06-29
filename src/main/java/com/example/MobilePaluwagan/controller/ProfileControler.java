package com.example.MobilePaluwagan.controller;


import com.example.MobilePaluwagan.dto.Request.ProfileUpdateRequest;
import com.example.MobilePaluwagan.dto.Response.AdminMemberListResponse;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.UserProfileResponse;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import com.example.MobilePaluwagan.service.MembersService;
import com.example.MobilePaluwagan.service.ProfileService;
import com.example.MobilePaluwagan.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileControler {


    private final ProfileService profileService;
    private final SupabaseStorageService supabaseStorageService;
    private final UserInfoRepo userInfoRepo;
    private final SseController sseController;
    private final MembersService membersService;

    @GetMapping("/info")
    public UserProfileResponse userAllInfo(Authentication authentication){
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        Long userid = userDetails.userId();

        UserProfileResponse response = profileService.userProfileInfo(userid);

        return response;
    }

    @GetMapping("/ledger")
    public ResponseEntity<?> getMember(Authentication authentication,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size){
        UserPrinciple details = (UserPrinciple) authentication.getPrincipal();
        Long userId = details.userId();

        try{
            AdminMemberListResponse response = membersService.userGetLedger(userId, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
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
        System.out.println("UserId: " + userId);

        String imageUrl = supabaseStorageService.uploadProfileImage(file, userId);

        UserInfo userInfo = userInfoRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userInfo.setProfileImage(imageUrl);
        userInfoRepo.save(userInfo);

        sseController.notifyUpdate();
        return ResponseEntity.ok(new ApiResponse<>(true, "Upload successful", imageUrl));
    }


    @GetMapping("/loan")
    public ResponseEntity<?> userAllLoans(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
            Long userId = userDetails.userId();

            Page<Ledger> payments = profileService.getAllPayments(userId, page, size);
            return ResponseEntity.ok(payments);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/savings")
    public ResponseEntity<?> userAllSavings(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
            Long userId = userDetails.userId();

            Page<Ledger> payments = profileService.getAllPayments(userId, page, size);
            return ResponseEntity.ok(payments);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }


}
