package com.example.MobilePaluwagan.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MembersFilterResponse {

    private String fullName;
    private String role;
    private LocalDate verifiedDate;


    public boolean hasFilters(){
        return  fullName != null && role != null;
    }
}
