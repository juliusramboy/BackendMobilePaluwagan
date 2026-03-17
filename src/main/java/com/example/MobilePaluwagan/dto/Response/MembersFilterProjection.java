package com.example.MobilePaluwagan.dto.Response;

import java.time.LocalDate;

public interface MembersFilterProjection {
    String getFullName();

    String getRole_name();

    LocalDate getVerified_date();

    Boolean getHas_savings_account();

    Boolean getHas_loan();
}