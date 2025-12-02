package com.java.automation.tests.user;

import com.java.automation.base.BaseTest;
import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for Login functionality with DataProvider
 * Demonstrates TestNG DataProvider for data-driven testing
 */
public class LoginTestWithDataProvider extends BaseTest {

    /**
     * DataProvider for login test data
     * Returns array of test data: [customerId, password, expectedResult]
     */
    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        return new Object[][] {
            // Valid login
            {TestConfig.getProperty("test.user.id"), TestConfig.getProperty("test.user.password"), "success"},
            // Invalid customer ID
            {"invalid_user", "123456", "error"},
            // Invalid password
            {TestConfig.getProperty("test.user.id"), "wrong_password", "error"},
            // Empty customer ID
            {"", "123456", "error"},
            // Empty password
            {TestConfig.getProperty("test.user.id"), "", "error"}
        };
    }

    @Test(dataProvider = "loginData", description = "Test đăng nhập với nhiều bộ dữ liệu khác nhau")
    public void testLoginWithDataProvider(String customerId, String password, String expectedResult) {
        extentTest.log(com.aventstack.extentreports.Status.INFO, 
            "Testing login with Customer ID: " + customerId + ", Expected: " + expectedResult);
        
        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();
        
        loginPage.login(customerId, password);
        
        if ("success".equals(expectedResult)) {
            Assert.assertTrue(loginPage.isOnHomePage(), 
                "Đăng nhập thành công nhưng không redirect về trang chủ");
            extentTest.log(com.aventstack.extentreports.Status.PASS, "Login successful");
        } else {
            // For error cases, should stay on login page or show error
            boolean isError = loginPage.isErrorAlertDisplayed() || loginPage.isOnLoginPage();
            Assert.assertTrue(isError, 
                "Không hiển thị lỗi khi đăng nhập với thông tin không hợp lệ");
            extentTest.log(com.aventstack.extentreports.Status.PASS, "Error handled correctly");
        }
    }
}

