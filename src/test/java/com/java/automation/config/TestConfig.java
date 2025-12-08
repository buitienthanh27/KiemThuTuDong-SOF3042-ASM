package com.java.automation.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TestConfig {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (input == null) {
                System.out.println("⚠️ Sorry, unable to find test.properties");
            } else {
                // Dùng UTF-8 để đọc tiếng Việt không bị lỗi font
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}