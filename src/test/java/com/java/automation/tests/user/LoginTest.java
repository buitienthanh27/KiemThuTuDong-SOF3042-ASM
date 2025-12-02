package com.java.automation.tests.user;

import com.java.automation.base.BaseTest;
import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for Login functionality
 */
public class LoginTest extends BaseTest {

    @Test(priority = 1, description = "Test đăng nhập thành công với thông tin hợp lệ")
    public void testLoginSuccess() {
        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();
        
        String customerId = TestConfig.getProperty("test.user.id");
        String password = TestConfig.getProperty("test.user.password");
        
        loginPage.login(customerId, password);
        
        // Verify redirect to home page after successful login
        Assert.assertTrue(loginPage.isOnHomePage(), 
            "Đăng nhập thành công nhưng không redirect về trang chủ");
    }

    @Test(priority = 2, description = "Test đăng nhập thất bại với Customer ID sai")
    public void testLoginWithInvalidCustomerId() {
        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();
        
        loginPage.login("invalid_user_id", "123456");
        
        // Verify error message is displayed
        Assert.assertTrue(loginPage.isErrorAlertDisplayed(), 
            "Không hiển thị thông báo lỗi khi đăng nhập với Customer ID sai");
        
        String errorText = loginPage.getErrorAlertText();
        Assert.assertTrue(errorText.contains("không chính xác") || 
                         errorText.contains("Tài khoản"), 
            "Thông báo lỗi không đúng: " + errorText);
    }

    @Test(priority = 3, description = "Test đăng nhập thất bại với mật khẩu sai")
    public void testLoginWithInvalidPassword() {
        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();
        
        String customerId = TestConfig.getProperty("test.user.id");
        loginPage.login(customerId, "wrong_password");
        
        // Verify error message is displayed
        Assert.assertTrue(loginPage.isErrorAlertDisplayed(), 
            "Không hiển thị thông báo lỗi khi đăng nhập với mật khẩu sai");
        
        String errorText = loginPage.getErrorAlertText();
        Assert.assertTrue(errorText.contains("không chính xác") || 
                         errorText.contains("Tài khoản"), 
            "Thông báo lỗi không đúng: " + errorText);
    }

    // Commented out to reduce test execution time
    // @Test(priority = 4, description = "Test đăng nhập thất bại với Customer ID và mật khẩu đều trống")
    // public void testLoginWithEmptyFields() {
    //     LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
    //     loginPage.navigateToLoginPage();
    //     
    //     loginPage.clickSignInTab();
    //     loginPage.clickSignInButton();
    //     
    //     // Verify that form validation prevents submission or shows error
    //     // HTML5 validation should prevent empty submission
    //     Assert.assertTrue(loginPage.isOnLoginPage(), 
    //         "Form không validate khi các trường trống");
    // }

    // @Test(priority = 5, description = "Test đăng nhập với Remember Me checkbox")
    // public void testLoginWithRememberMe() {
    //     LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
    //     loginPage.navigateToLoginPage();
    //     
    //     String customerId = TestConfig.getProperty("test.user.id");
    //     String password = TestConfig.getProperty("test.user.password");
    //     
    //     loginPage.clickSignInTab();
    //     loginPage.enterLoginCustomerId(customerId);
    //     loginPage.enterLoginPassword(password);
    //     loginPage.checkRememberMe();
    //     loginPage.clickSignInButton();
    //     
    //     // Verify successful login
    //     Assert.assertTrue(loginPage.isOnHomePage(), 
    //         "Đăng nhập với Remember Me không thành công");
    // }

    // @Test(priority = 6, description = "Test đăng nhập với Customer ID trống")
    // public void testLoginWithEmptyCustomerId() {
    //     LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
    //     loginPage.navigateToLoginPage();
    //     
    //     loginPage.clickSignInTab();
    //     loginPage.enterLoginPassword("123456");
    //     loginPage.clickSignInButton();
    //     
    //     // HTML5 validation should prevent submission
    //     Assert.assertTrue(loginPage.isOnLoginPage(), 
    //         "Form không validate khi Customer ID trống");
    // }

    // @Test(priority = 7, description = "Test đăng nhập với mật khẩu trống")
    // public void testLoginWithEmptyPassword() {
    //     LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
    //     loginPage.navigateToLoginPage();
    //     
    //     loginPage.clickSignInTab();
    //     loginPage.enterLoginCustomerId("testuser001");
    //     loginPage.clickSignInButton();
    //     
    //     // HTML5 validation should prevent submission
    //     Assert.assertTrue(loginPage.isOnLoginPage(), 
    //         "Form không validate khi mật khẩu trống");
    // }
}

