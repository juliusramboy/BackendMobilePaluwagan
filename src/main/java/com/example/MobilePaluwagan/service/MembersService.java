package com.example.MobilePaluwagan.service;

import com.example.MobilePaluwagan.dto.Response.MembersFilterProjection;
import com.example.MobilePaluwagan.dto.Response.MembersFilterResponse;
import com.example.MobilePaluwagan.entity.UserInfo;
import com.example.MobilePaluwagan.repository.UserInfoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MembersService {


    private final UserInfoRepo userInfoRepo;

    public MembersService(UserInfoRepo userInfoRepo) {
        this.userInfoRepo = userInfoRepo;
    }

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
}
