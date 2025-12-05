package com.java.automation.selenium;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.util.Optional;

public class TestListener implements TestWatcher {

    // Bạn cần sửa hàm này để lấy WebDriver từ BaseTest của bạn
    private WebDriver getDriverFromContext(ExtensionContext context) {
        // Ví dụ: Nếu class test của bạn kế thừa BaseTest, hãy cast nó
        // BaseTest instance = (BaseTest) context.getRequiredTestInstance();
        // return instance.getDriver();

        // HOẶC: Nếu bạn dùng biến static driver (cách đơn giản nhất để demo)
        // return BaseTest.driver;
        return null; // Hãy thay thế dòng này bằng code lấy driver thực tế của bạn
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        WebDriver driver = getDriverFromContext(context);
        if (driver != null) {
            try {
                // Chụp ảnh và đính kèm vào Allure Report
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Screenshot on Failure", new ByteArrayInputStream(screenshot));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}