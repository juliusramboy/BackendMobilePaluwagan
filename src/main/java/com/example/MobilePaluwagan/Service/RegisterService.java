package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.DTOs.Request.RegisterRequest;
import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserInfo;
import com.example.MobilePaluwagan.Repository.UserInfoRepo;
import com.example.MobilePaluwagan.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserInfoRepo userInfoRepo;

    public void register(RegisterRequest register){

        UserInfo userInfo = new UserInfo();
        userInfo.setFirstName(register.getFirstName());
        userInfo.setMiddleName(register.getMiddleName());
        userInfo.setLastName(register.getLastName());
        userInfo.setEmail(register.getEmail());
        userInfo.setPhoneNumber(register.getPhoneNumber());
        userInfoRepo.save(userInfo);

        User userAcc = new User();
        userAcc.setUsername(register.getUsername());
        userAcc.setPassword(register.getPassword());

    }

}
