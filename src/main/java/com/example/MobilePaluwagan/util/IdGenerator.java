package com.example.MobilePaluwagan.util;

import java.security.SecureRandom;
import java.util.Random;

public class IdGenerator {
    // Exclude confusing characters like I, O, L, 0, 1
    private static final String CLEAN_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Random RANDOM = new SecureRandom();

    /**
     * Generates a short unique alphanumeric string of specified length.
     * With length 8 and 32 characters alphabet, combinations are 32^8 ≈ 1.1 Trillion.
     */
    public static String generateShortCode(String prefix, int length) {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < length; i++) {
            sb.append(CLEAN_ALPHABET.charAt(RANDOM.nextInt(CLEAN_ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * Generates a unique short numeric ID (8 digits long).
     */
    public static Long generateShortNumericId() {
        // Generates a random number in range [10000000, 99999999]
        long number = 10000000L + RANDOM.nextInt(90000000);
        return number;
    }
}
