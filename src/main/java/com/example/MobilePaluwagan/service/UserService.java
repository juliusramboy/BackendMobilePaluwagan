package com.example.MobilePaluwagan.service;


import com.example.MobilePaluwagan.repository.UserInfoRepo;
import com.example.MobilePaluwagan.repository.UserSavingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserInfoRepo userInfoRepo;
    @Autowired
    private UserSavingsRepo userSavingsRepo;

//    public String findUsername(Long userId){
//        UserInfo userInfo = userInfoRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//        UserSavings userSavings = userSavingsRepo.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User savings not found"));
//        return userInfo.getFirstName() + " " + userInfo.getLastName() + " " + userSavings.getAmountDeposit() + " " + userSavings.getDepositDate() + " " + userSavings.getReference();
//    }
}
