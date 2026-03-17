package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Request.RegisterRequest;
import com.example.MobilePaluwagan.dto.Response.MembersFilterProjection;
import com.example.MobilePaluwagan.dto.Response.MembersFilterResponse;
import com.example.MobilePaluwagan.entity.*;
import com.example.MobilePaluwagan.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class MembersService {


    private final UserInfoRepo userInfoRepo;

    private final RoleRepo roleRepo;

    private final UserRepo userRepo;

    private final UserBankRepo userBankRepo;

    private final VerificationRepo verificationRepo;

    private final RegisterService registerService;

    public MembersService(UserInfoRepo userInfoRepo,  RoleRepo roleRepo, UserRepo userRepo, UserBankRepo userBankRepo,  VerificationRepo verificationRepo,  RegisterService registerService) {

        this.userInfoRepo = userInfoRepo;
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.userBankRepo = userBankRepo;
        this.verificationRepo = verificationRepo;
        this.registerService = registerService;
    }

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Page<MembersFilterProjection> filterMembers(MembersFilterResponse filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // ← Convert empty string to null
        String name = (filter.getName() != null && !filter.getName().isEmpty())
                ? filter.getName() : null;
        String surname = (filter.getSurname() != null && !filter.getSurname().isEmpty())
                ? filter.getSurname() : null;
        String role = (filter.getRole() != null && !filter.getRole().isEmpty())
                ? filter.getRole() : null;

        return userInfoRepo.filterMembers(name, surname, role, pageable);
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

        userInfoRepo.save(userInfo);

        UserBank userBank = new UserBank();
        userBank.setUserId(userdataWithId.getId());
        userBank.setAccountBalance(BigDecimal.valueOf(0L));

        userBankRepo.save(userBank);

        return new ResponseEntity<>(userdataWithId.getId(), HttpStatus.OK);
    }
}
