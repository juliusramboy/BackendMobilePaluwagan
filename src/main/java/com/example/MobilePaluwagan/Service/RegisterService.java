package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Request.RegisterRequest;
import com.example.MobilePaluwagan.Entity.Role;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserInfo;
import com.example.MobilePaluwagan.Repository.RoleRepo;
import com.example.MobilePaluwagan.Repository.UserInfoRepo;
import com.example.MobilePaluwagan.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserInfoRepo userInfoRepo;

    @Autowired
    private RoleRepo roleRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public void register(RegisterRequest register) {


        UserInfo userInfo = new UserInfo();
        userInfo.setFirstName(register.getFirstName());
        userInfo.setMiddleName(register.getMiddleName());
        userInfo.setLastName(register.getLastName());
        userInfo.setEmail(register.getEmail());
        userInfo.setPhoneNumber(register.getPhoneNumber());

        User userAcc = new User();
        userAcc.setUsername(register.getUsername());
        userAcc.setPassword(encoder.encode(register.getPassword()));

        Role defaultRole = roleRepo.findById(2)
                .orElseThrow(() -> new RuntimeException("Error: Default role (ID 2) not found in database."));

        userAcc.setRole(defaultRole);
        userAcc.setUserInfo(userInfo);
        userRepo.save(userAcc);
    }
}