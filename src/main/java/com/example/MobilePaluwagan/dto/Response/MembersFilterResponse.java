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

    private String name;
    private String surname;
    private String role;
    private LocalDate verifiedDate;


    public boolean hasFilters(){
        return name != null && surname != null && role != null;
    }
}
