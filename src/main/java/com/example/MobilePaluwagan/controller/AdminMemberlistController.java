package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.dto.Request.ProfileUpdateRequest;
import com.example.MobilePaluwagan.dto.Request.RegisterRequest;
import com.example.MobilePaluwagan.dto.Response.AdminMemberListResponse;
import com.example.MobilePaluwagan.dto.Response.ApiResponse;
import com.example.MobilePaluwagan.dto.Response.MembersFilterResponse;
import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.entity.UserInfo;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import com.example.MobilePaluwagan.service.MembersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/memberlist")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberlistController {


    private final MembersService membersService;

    public AdminMemberlistController(MembersService membersService) {
        this.membersService = membersService;
    }


    @GetMapping("/search")
    public ResponseEntity<?> searchMemberlist(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        MembersFilterResponse filter = MembersFilterResponse.builder()
                .fullName(fullName)
                .role(role)
                .build();

        return ResponseEntity.ok(membersService.filterMembers(filter, page, size));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody RegisterRequest request){
        return membersService.adminRegister(request);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getMember(@PathVariable Long userId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size){
        try{
            AdminMemberListResponse response = membersService.getAdminUserProfile(userId, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/member/{userId}")
    public ResponseEntity<?> deleteMember(@PathVariable Long userId){
        return membersService.deleteMember(userId);
    }

    @PatchMapping("/member/{userId}/edit")
    public ApiResponse<String> editInfoMember(@PathVariable Long userId, @RequestBody ProfileUpdateRequest request){return membersService.updateProfile(userId, request);}
}
