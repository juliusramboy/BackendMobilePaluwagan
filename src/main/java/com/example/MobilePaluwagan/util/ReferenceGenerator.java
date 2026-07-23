package com.example.MobilePaluwagan.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.security.SecureRandom;

public class ReferenceGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(String typeCode) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return "REF-" + typeCode + "-" + datePart + "-" + sb.toString();
    }

    public static String generateSavingsId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return "SID-" + datePart + "-" + sb.toString();
    }
}
