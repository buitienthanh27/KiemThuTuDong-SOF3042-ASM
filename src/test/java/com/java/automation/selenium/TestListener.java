package com.java.automation.selenium;

import com.java.automation.utils.ExtentReportManager;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        ExtentReportManager.createTest(result.getName(), result.getMethod().getDescription());
        System.out.println("--- BẮT ĐẦU TEST: " + result.getName() + " ---");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (ExtentReportManager.getTest() != null) {
            ExtentReportManager.getTest().log(Status.PASS, "Test Passed: " + result.getName());
        }
        System.out.println("✅ TEST PASSED: " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("❌ TEST FAILED: " + result.getName());

        try {
            Object currentClass = result.getInstance();
            WebDriver driver = ((BaseSeleniumTest) currentClass).getDriver();

            if (driver != null) {
                System.out.println("📸 Đang gọi hàm chụp ảnh cho test: " + result.getName());
                String screenshotPath = ((BaseSeleniumTest) currentClass).takeScreenshot(result.getName());

                // Attach vào Allure Report (nếu có dùng)
                if (ExtentReportManager.getTest() != null && screenshotPath != null) {
                    ExtentReportManager.getTest().addScreenCaptureFromPath(screenshotPath);
                }
            } else {
                System.out.println("⚠️ Driver bị NULL, không thể chụp ảnh.");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Lỗi Listener khi chụp ảnh: " + e.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("⚠️ TEST SKIPPED: " + result.getName());
        if (ExtentReportManager.getTest() != null) {
            ExtentReportManager.getTest().log(Status.SKIP, "Test Skipped: " + result.getName());
            if (result.getThrowable() != null) {
                ExtentReportManager.getTest().log(Status.SKIP, result.getThrowable());
            }
        }

        // Thử chụp ảnh ngay cả khi Skipped (thường do lỗi setup)
        try {
            Object currentClass = result.getInstance();
            if (currentClass instanceof BaseSeleniumTest) {
                ((BaseSeleniumTest) currentClass).takeScreenshot(result.getName() + "_Skipped");
            }
        } catch (Exception ignored) {}
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

    @Override public void onStart(ITestContext context) {}

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
        System.out.println("📝 Extent Report generated in test-output/reports/");
        System.out.println("--- KẾT THÚC BỘ TEST ---");
    }
}