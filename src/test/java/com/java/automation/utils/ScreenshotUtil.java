package com.java.automation.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for taking screenshots
 */
public class ScreenshotUtil {
    private static final String SCREENSHOT_DIR = "test-output/screenshots/";

    /**
     * Take screenshot and save to file
     * @param driver WebDriver instance
     * @param screenshotName Name of the screenshot file (without extension)
     * @return File path of the screenshot
     */
    public static String takeScreenshot(WebDriver driver, String screenshotName) {
        try {
            // Create screenshot directory if it doesn't exist
            File directory = new File(SCREENSHOT_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate timestamp for unique filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = screenshotName + "_" + timestamp + ".png";
            String filePath = SCREENSHOT_DIR + fileName;

            // Take screenshot
            TakesScreenshot ts = (TakesScreenshot) driver;
            File sourceFile = ts.getScreenshotAs(OutputType.FILE);
            File destinationFile = new File(filePath);
            FileUtils.copyFile(sourceFile, destinationFile);

            return filePath;
        } catch (IOException e) {
            System.err.println("Error taking screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Take screenshot with default name
     * @param driver WebDriver instance
     * @return File path of the screenshot
     */
    public static String takeScreenshot(WebDriver driver) {
        return takeScreenshot(driver, "screenshot");
    }
}

