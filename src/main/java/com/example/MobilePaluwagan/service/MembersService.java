package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Request.RegisterRequest;
import com.example.MobilePaluwagan.dto.Response.AdminMemberListResponse;
import com.example.MobilePaluwagan.dto.Response.MembersFilterProjection;
import com.example.MobilePaluwagan.dto.Response.MembersFilterResponse;
import com.example.MobilePaluwagan.dto.Response.UserProfileResponse;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MembersService {


    private final UserInfoRepo userInfoRepo;
    private final RoleRepo roleRepo;
    private final UserRepo userRepo;
    private final UserBankRepo userBankRepo;
    private final LoanPaymentRepo loanPaymentRepo;
    private final UserSavingsRepo userSavingsRepo;
    private final ProfileService profileService;



    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Page<MembersFilterProjection> filterMembers(MembersFilterResponse filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // ← Convert empty string to null
        String fullName  = (filter.getFullName() != null && !filter.getFullName().isEmpty())
                ? filter.getFullName() : null;
        String role = (filter.getRole() != null && !filter.getRole().isEmpty())
                ? filter.getRole() : null;

        return userInfoRepo.filterMembers(fullName, role, pageable);
    }


    public ResponseEntity<Long> adminRegister(RegisterRequest register) {

        Role defaultRole = roleRepo.findById(1)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));


        String rawPassword = register.getPassword();
        String hashedPassword = encoder.encode(rawPassword);
        User user = new User();
        user.setEmail(register.getEmail());
        user.setPassword(hashedPassword);
        user.setRole(defaultRole);
        user.setActive(true);

        User userdataWithId = userRepo.save(user);


        UserVerification userVerification = new UserVerification();
        userVerification.setUserId(userdataWithId.getId());

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userdataWithId.getId());
        userInfo.setFirstName(register.getFirstName());
        userInfo.setMiddleName(register.getMiddleName());
        userInfo.setLastName(register.getLastName());
        userInfo.setSuffix(register.getSuffix());
        userInfo.setPhoneNumber(register.getPhoneNumber());
        userInfo.setVerifiedDate(LocalDate.now());

        userInfoRepo.save(userInfo);

        UserBank userBank = new UserBank();
        userBank.setUserId(userdataWithId.getId());
        userBank.setAccountBalance(BigDecimal.valueOf(0L));

        userBankRepo.save(userBank);

        return new ResponseEntity<>(userdataWithId.getId(), HttpStatus.OK);
    }

//    public void deleteMember(Long userId){
//
//        dueDateScheduleRepo.findByUserId(userId);
//        ledgerRepo.deleteById(userId);
//        notificationRepo.deleteById(userId);
//        userBankRepo.deleteById(userId);
//        userInfoRepo.deleteById(userId);
//        userRepo.deleteById(userId);
//
//    }

    public AdminMemberListResponse getAdminUserProfile(Long userId, int page, int size) {
        UserProfileResponse info = profileService.userAllInfo(userId);
        Page<Ledger> ledgerPayments = profileService.getAllPayments(userId, page, size);

        return AdminMemberListResponse.builder()
                .info(info)
                .allPayments(ledgerPayments)
                .build();
    }
}
