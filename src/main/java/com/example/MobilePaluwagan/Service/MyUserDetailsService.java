package com.example.MobilePaluwagan.Service;

import com.example.MobilePaluwagan.Entity.User;
import com.example.MobilePaluwagan.Entity.UserPrinciple;
import com.example.MobilePaluwagan.Repository.UserRepo;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = repo.findByUsername(username);

        if (user == null){
            System.out.println("username not found");
            throw new RuntimeException("username not found");
        }
        return new UserPrinciple(user);
    }
}
