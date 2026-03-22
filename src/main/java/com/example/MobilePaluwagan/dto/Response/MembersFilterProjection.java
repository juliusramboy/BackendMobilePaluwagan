package com.example.MobilePaluwagan.dto.Response;

import java.time.LocalDate;

public interface MembersFilterProjection {
    Long getUser_id();
    String getFirst_name();
    String getProfile_image();
    String getLast_name();
    String getRole_name();

    LocalDate getVerified_date();

    Boolean getHas_savings_account();

    Boolean getHas_loan();
}