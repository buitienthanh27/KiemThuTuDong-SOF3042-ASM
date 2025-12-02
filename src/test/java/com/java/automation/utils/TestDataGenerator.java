package com.java.automation.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Utility class for generating test data
 */
public class TestDataGenerator {
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Generate unique customer ID
     */
    public static String generateUniqueCustomerId() {
        return "testuser_" + LocalDateTime.now().format(formatter) + "_" + random.nextInt(1000);
    }

    /**
     * Generate unique email
     */
    public static String generateUniqueEmail() {
        return "test_" + LocalDateTime.now().format(formatter) + "_" + random.nextInt(1000) + "@example.com";
    }

    /**
     * Generate unique fullname
     */
    public static String generateUniqueFullname() {
        return "Test User " + LocalDateTime.now().format(formatter);
    }

    /**
     * Generate random string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

