package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.entity.User;
import com.example.MobilePaluwagan.entity.UserPrinciple;
import com.example.MobilePaluwagan.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepo repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = repo.findByEmail(email);

        if (user == null){
            System.out.println("username not found");
            throw new RuntimeException("username not found");
        }
        return new UserPrinciple(user);
    }
}
