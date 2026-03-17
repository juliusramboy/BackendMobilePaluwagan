package com.example.MobilePaluwagan.controller;

import com.example.MobilePaluwagan.dto.Response.MembersFilterResponse;
import com.example.MobilePaluwagan.entity.UserInfo;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import com.example.MobilePaluwagan.service.MembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        MembersFilterResponse filter = MembersFilterResponse.builder()
                .name(name)
                .surname(surname)
                .role(role)
                .build();

        return ResponseEntity.ok(membersService.filterMembers(filter, page, size));
    }
}
