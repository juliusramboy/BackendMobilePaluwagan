package com.example.MobilePaluwagan.Controller;


import com.example.MobilePaluwagan.Entity.UserPrinciple;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileControler {

//    @GetMapping("/profile/info")
//    public ResponseEntity<?> userAllInfo(Authentication authentication){
//        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
//        Long userid = userDetails.userId();
//
//        return userid;
//    }
}
