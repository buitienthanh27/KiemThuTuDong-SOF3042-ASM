package com.java.automation.selenium;

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

// SỬA TÊN CLASS: Thêm chữ 'e' vào cuối cho khớp với tên file TestListener.java
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("❌ Test Failed: " + result.getName());

        // Gọi driver từ BaseSeleniumTest
        if (BaseSeleniumTest.driver != null) {
            try {
                // 1. Chụp ảnh lưu file
                File src = ((TakesScreenshot) BaseSeleniumTest.driver).getScreenshotAs(OutputType.FILE);

                String methodName = result.getName();
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "screenshots/FAIL_" + methodName + "_" + time + ".png";

                Path dest = Paths.get(fileName);
                Files.createDirectories(dest.getParent());
                Files.copy(src.toPath(), dest);
                System.out.println("📸 Screenshot saved: " + dest.toAbsolutePath());

                // 2. Đính kèm vào Allure Report
                byte[] content = ((TakesScreenshot) BaseSeleniumTest.driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(methodName + "_Failure", new ByteArrayInputStream(content));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override public void onTestStart(ITestResult result) {}
    @Override public void onTestSuccess(ITestResult result) {}
    @Override public void onTestSkipped(ITestResult result) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onStart(org.testng.ITestContext context) {}
    @Override public void onFinish(org.testng.ITestContext context) {}
}