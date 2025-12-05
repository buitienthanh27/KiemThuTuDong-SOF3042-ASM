package com.java.automation.selenium;

import com.java.automation.selenium.BaseSeleniumTest;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListenr implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("❌ Test Failed: " + result.getName());

        // Gọi driver từ BaseSeleniumTest
        if (BaseSeleniumTest.driver != null) {
            try {
                // 1. Chụp ảnh lưu file (để xem trong Artifacts Github)
                File src = ((TakesScreenshot) BaseSeleniumTest.driver).getScreenshotAs(OutputType.FILE);

                String methodName = result.getName();
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "screenshots/FAIL_" + methodName + "_" + time + ".png";

                Path dest = Paths.get(fileName);
                Files.createDirectories(dest.getParent());
                Files.copy(src.toPath(), dest);
                System.out.println("📸 Screenshot saved: " + dest.toAbsolutePath());

                // 2. Đính kèm vào Allure Report (Quan trọng để xem trên web)
                byte[] content = ((TakesScreenshot) BaseSeleniumTest.driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(methodName + "_Failure", new ByteArrayInputStream(content));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Các method khác của ITestListener có thể để trống nếu không dùng
    @Override public void onTestStart(ITestResult result) {}
    @Override public void onTestSuccess(ITestResult result) {}
    @Override public void onTestSkipped(ITestResult result) {}
}