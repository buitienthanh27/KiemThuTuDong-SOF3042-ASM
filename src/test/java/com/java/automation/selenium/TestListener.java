package com.java.automation.selenium;

import com.java.automation.utils.ExtentReportManager; // Import file quản lý báo cáo
import com.aventstack.extentreports.Status; // Import trạng thái báo cáo
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener {

    // 1. Khi bắt đầu 1 Test Case -> Tạo dòng mới trong báo cáo
    @Override
    public void onTestStart(ITestResult result) {
        ExtentReportManager.createTest(result.getName(), result.getMethod().getDescription());
    }

    // 2. Khi Test PASS -> Ghi log màu xanh
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentReportManager.getTest().log(Status.PASS, "Test Passed: " + result.getName());
    }

    // 3. Khi Test FAIL -> Ghi log màu đỏ + Chụp ảnh
    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("❌ Test Failed: " + result.getName());
        ExtentReportManager.getTest().log(Status.FAIL, "Test Failed: " + result.getName());
        ExtentReportManager.getTest().log(Status.FAIL, result.getThrowable()); // Ghi lỗi chi tiết vào báo cáo

        if (BaseSeleniumTest.driver != null) {
            try {
                // --- Phần chụp ảnh cũ của bạn (Giữ nguyên) ---
                File src = ((TakesScreenshot) BaseSeleniumTest.driver).getScreenshotAs(OutputType.FILE);
                String methodName = result.getName();
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "screenshots/FAIL_" + methodName + "_" + time + ".png";
                Path dest = Paths.get(fileName);
                Files.createDirectories(dest.getParent());
                Files.copy(src.toPath(), dest);

                // Đính ảnh vào Allure (Giữ nguyên)
                byte[] content = ((TakesScreenshot) BaseSeleniumTest.driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(methodName + "_Failure", new ByteArrayInputStream(content));

                // --- THÊM MỚI: Đính ảnh vào ExtentReport ---
                // ExtentReportManager.getTest().addScreenCaptureFromPath(dest.toAbsolutePath().toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override public void onTestSkipped(ITestResult result) {
        ExtentReportManager.getTest().log(Status.SKIP, "Test Skipped: " + result.getName());
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

    @Override public void onStart(ITestContext context) {}

    // 4. QUAN TRỌNG NHẤT: Lưu file báo cáo khi chạy xong tất cả
    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
        System.out.println("📝 Extent Report generated in test-output/reports/");
    }
}