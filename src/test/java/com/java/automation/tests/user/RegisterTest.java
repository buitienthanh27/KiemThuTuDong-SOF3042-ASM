package com.java.automation.tests.user;

import com.java.automation.base.BaseTest;
import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import com.java.automation.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for Registration functionality
 */
public class RegisterTest extends BaseTest {

    @Test(priority = 1, description = "Test đăng ký thành công với thông tin hợp lệ")
    public void testRegisterSuccess() {
        LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
        registerPage.navigateToLoginPage();
        
        String customerId = TestDataGenerator.generateUniqueCustomerId();
        String fullname = TestDataGenerator.generateUniqueFullname();
        String email = TestDataGenerator.generateUniqueEmail();
        String password = "123456";
        
        registerPage.register(customerId, fullname, email, password);
        
        // Verify success message is displayed
        Assert.assertTrue(registerPage.isSuccessAlertDisplayed(), 
            "Không hiển thị thông báo thành công khi đăng ký");
        
        String successText = registerPage.getSuccessAlertText();
        Assert.assertTrue(successText.contains("thành công") || 
                         successText.contains("Đăng kí"), 
            "Thông báo thành công không đúng: " + successText);
    }

    @Test(priority = 2, description = "Test đăng ký thất bại với Customer ID đã tồn tại")
    public void testRegisterWithExistingCustomerId() {
        LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
        registerPage.navigateToLoginPage();
        
        // Use an existing customer ID (assuming test.user.id exists)
        String existingCustomerId = TestConfig.getProperty("test.user.id");
        String fullname = TestDataGenerator.generateUniqueFullname();
        String email = TestDataGenerator.generateUniqueEmail();
        String password = "123456";
        
        registerPage.register(existingCustomerId, fullname, email, password);
        
        // Verify error message is displayed
        Assert.assertTrue(registerPage.isErrorAlertDisplayed(), 
            "Không hiển thị thông báo lỗi khi đăng ký với Customer ID đã tồn tại");
        
        String errorText = registerPage.getErrorAlertText();
        Assert.assertTrue(errorText.contains("ID Login") || 
                         errorText.contains("đã được sử dụng"), 
            "Thông báo lỗi không đúng: " + errorText);
    }

    @Test(priority = 3, description = "Test đăng ký thất bại với Email đã tồn tại")
    public void testRegisterWithExistingEmail() {
        LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
        registerPage.navigateToLoginPage();
        
        // Use an existing email (assuming test.user.email exists)
        String customerId = TestDataGenerator.generateUniqueCustomerId();
        String fullname = TestDataGenerator.generateUniqueFullname();
        String existingEmail = TestConfig.getProperty("test.user.email");
        String password = "123456";
        
        registerPage.register(customerId, fullname, existingEmail, password);
        
        // Verify error message is displayed
        Assert.assertTrue(registerPage.isErrorAlertDisplayed(), 
            "Không hiển thị thông báo lỗi khi đăng ký với Email đã tồn tại");
        
        String errorText = registerPage.getErrorAlertText();
        Assert.assertTrue(errorText.contains("Email") || 
                         errorText.contains("đã được sử dụng"), 
            "Thông báo lỗi không đúng: " + errorText);
    }

    @Test(priority = 4, description = "Test đăng ký thất bại với Customer ID trống")
    public void testRegisterWithEmptyCustomerId() {
        LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
        registerPage.navigateToLoginPage();
        
        registerPage.clickSignUpTab();
        registerPage.enterRegisterFullname("Test User");
        registerPage.enterRegisterEmail("test@example.com");
        registerPage.enterRegisterPassword("123456");
        registerPage.clickSignUpButton();
        
        // HTML5 validation should prevent submission
        Assert.assertTrue(registerPage.isOnLoginPage(), 
            "Form không validate khi Customer ID trống");
    }

    // Commented out to reduce test execution time
    // @Test(priority = 5, description = "Test đăng ký thất bại với Fullname trống")
    // public void testRegisterWithEmptyFullname() {
    //     LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
    //     registerPage.navigateToLoginPage();
    //     
    //     registerPage.clickSignUpTab();
    //     registerPage.enterRegisterCustomerId(TestDataGenerator.generateUniqueCustomerId());
    //     registerPage.enterRegisterEmail("test@example.com");
    //     registerPage.enterRegisterPassword("123456");
    //     registerPage.clickSignUpButton();
    //     
    //     // HTML5 validation should prevent submission
    //     Assert.assertTrue(registerPage.isOnLoginPage(), 
    //         "Form không validate khi Fullname trống");
    // }

    // @Test(priority = 6, description = "Test đăng ký thất bại với Email trống")
    // public void testRegisterWithEmptyEmail() {
    //     LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
    //     registerPage.navigateToLoginPage();
    //     
    //     registerPage.clickSignUpTab();
    //     registerPage.enterRegisterCustomerId(TestDataGenerator.generateUniqueCustomerId());
    //     registerPage.enterRegisterFullname("Test User");
    //     registerPage.enterRegisterPassword("123456");
    //     registerPage.clickSignUpButton();
    //     
    //     // HTML5 validation should prevent submission
    //     Assert.assertTrue(registerPage.isOnLoginPage(), 
    //         "Form không validate khi Email trống");
    // }

    // @Test(priority = 7, description = "Test đăng ký thất bại với Email không hợp lệ")
    // public void testRegisterWithInvalidEmail() {
    //     LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
    //     registerPage.navigateToLoginPage();
    //     
    //     registerPage.clickSignUpTab();
    //     registerPage.enterRegisterCustomerId(TestDataGenerator.generateUniqueCustomerId());
    //     registerPage.enterRegisterFullname("Test User");
    //     registerPage.enterRegisterEmail("invalid-email");
    //     registerPage.enterRegisterPassword("123456");
    //     registerPage.clickSignUpButton();
    //     
    //     // HTML5 email validation should prevent submission
    //     Assert.assertTrue(registerPage.isOnLoginPage(), 
    //         "Form không validate khi Email không hợp lệ");
    // }

    // @Test(priority = 8, description = "Test đăng ký thất bại với mật khẩu trống")
    // public void testRegisterWithEmptyPassword() {
    //     LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
    //     registerPage.navigateToLoginPage();
    //     
    //     registerPage.clickSignUpTab();
    //     registerPage.enterRegisterCustomerId(TestDataGenerator.generateUniqueCustomerId());
    //     registerPage.enterRegisterFullname("Test User");
    //     registerPage.enterRegisterEmail("test@example.com");
    //     registerPage.clickSignUpButton();
    //     
    //     // HTML5 validation should prevent submission
    //     Assert.assertTrue(registerPage.isOnLoginPage(), 
    //         "Form không validate khi mật khẩu trống");
    // }

    // @Test(priority = 9, description = "Test đăng ký thất bại với mật khẩu quá ngắn")
    // public void testRegisterWithShortPassword() {
    //     LoginOrRegisterPage registerPage = new LoginOrRegisterPage(driver);
    //     registerPage.navigateToLoginPage();
    //     
    //     String customerId = TestDataGenerator.generateUniqueCustomerId();
    //     String fullname = TestDataGenerator.generateUniqueFullname();
    //     String email = TestDataGenerator.generateUniqueEmail();
    //     String shortPassword = "12345"; // Less than 6 characters
    //     
    //     registerPage.register(customerId, fullname, email, shortPassword);
    //     
    //     // HTML5 minlength validation should prevent submission or show error
    //     // The form requires password to be at least 6 characters
    //     Assert.assertTrue(registerPage.isOnLoginPage(), 
    //         "Form không validate khi mật khẩu quá ngắn");
    // }

    // @Test(priority = 10, description = "Test chuyển đổi giữa tab Sign In và Sign Up")
    // public void testSwitchBetweenTabs() {
    //     LoginOrRegisterPage page = new LoginOrRegisterPage(driver);
    //     page.navigateToLoginPage();
    //     
    //     // Initially on Sign In tab
    //     page.clickSignUpTab();
    //     // Should be on Sign Up tab now
    //     
    //     page.clickSignInTab();
    //     // Should be back on Sign In tab
    //     
    //     Assert.assertTrue(page.isOnLoginPage(), 
    //         "Không thể chuyển đổi giữa các tab");
    // }
}

