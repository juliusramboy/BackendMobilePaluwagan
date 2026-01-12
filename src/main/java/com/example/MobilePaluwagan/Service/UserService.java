package com.example.MobilePaluwagan.Service;


import com.example.MobilePaluwagan.Entity.UserInfo;
import com.example.MobilePaluwagan.Entity.UserSavings;
import com.example.MobilePaluwagan.Repository.UserInfoRepo;
import com.example.MobilePaluwagan.Repository.UserRepo;
import com.example.MobilePaluwagan.Repository.UserSavingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    @Autowired
    private UserInfoRepo userInfoRepo;
    @Autowired
    private UserSavingsRepo userSavingsRepo;

    public String findUsername(Long userId){
        UserInfo userInfo = userInfoRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserSavings userSavings = userSavingsRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User savings not found"));
        return userInfo.getFirstName() + " " + userInfo.getLastName() + " " + userSavings.getSavingsAmount() + " " + userSavings.getDepositDate() + " " + userSavings.getReference();
    }
}
