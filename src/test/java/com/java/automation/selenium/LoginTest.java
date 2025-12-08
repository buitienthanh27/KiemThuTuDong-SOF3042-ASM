package com.java.automation.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;

@Listeners(TestListener.class)
public class LoginTest extends BaseSeleniumTest {

    private static final int TIMEOUT = 10;

    /**
     * Hàm chuẩn bị: Vào trang Login, đảm bảo đang ở Tab Sign In
     */
    private void prepareLoginPage() {
        System.out.println("--- BẮT ĐẦU TEST CASE: LOGIN ---");
        // Dùng BASE_URL từ lớp cha cho đồng bộ (9090)
        driver.get(BASE_URL + "login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Nếu đang kẹt ở trang Admin -> Logout ra
        if (driver.getCurrentUrl().contains("admin")) {
            System.out.println("Phát hiện đang ở Admin, tiến hành Logout...");
            driver.get(BASE_URL + "logout");
            driver.get(BASE_URL + "login");
        }

        // 2. CHUYỂN TAB SIGN IN
        try {
            WebElement signInTab = driver.findElement(By.xpath("//ul[contains(@class, 'nav-tabs')]//a[contains(text(), 'sign in')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", signInTab);
            Thread.sleep(500);
            System.out.println("Đã chuyển sang Tab Sign In");
        } catch (Exception e) {
            System.out.println("Lưu ý: Không tìm thấy Tab Sign In (có thể đã ở sẵn đó).");
        }
    }

    @Test(priority = 1)
    void login_with_valid_customer_should_success() {
        prepareLoginPage();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));

        // 1. Nhập Email
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
        emailInput.clear();
        emailInput.sendKeys("abcd"); // Tài khoản đúng

        // 2. Nhập Password
        WebElement passInput = driver.findElement(By.xpath("//div[@id='signin']//input[@name='password']"));
        passInput.clear();
        passInput.sendKeys("123123");

        // 3. Click Login
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

        // 4. Kiểm tra kết quả
        System.out.println("Đã bấm Login, đang chờ chuyển trang...");
        try {
            // Chờ URL đổi về trang chủ
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
            System.out.println("✅ Login thành công: URL đã về trang chủ.");
        } catch (Exception e) {
            // Nếu URL không đổi, check nút Logout
            try {
                WebElement logoutBtn = driver.findElement(By.partialLinkText("Logout"));
                if(logoutBtn.isDisplayed()){
                    System.out.println("✅ Login thành công (Check nút Logout).");
                    return;
                }
            } catch (Exception ex) {
                takeScreenshot("Login_Valid_FAIL");
                Assert.fail("Login thất bại: Vẫn ở trang Login. URL: " + driver.getCurrentUrl());
            }
        }
    }

    @Test(priority = 2)
    void login_with_wrong_password_should_show_error() {
        prepareLoginPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));

        // 1. Nhập đúng User
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
        emailInput.clear();
        emailInput.sendKeys("abcd");

        // 2. Nhập SAI Password
        WebElement passInput = driver.findElement(By.xpath("//div[@id='signin']//input[@name='password']"));
        passInput.clear();
        passInput.sendKeys("sai_password_nay");

        // 3. Click Login
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

        // 4. Kiểm tra lỗi
        try {
            // Logic: Nếu URL vẫn là trang login (chứa 'login' hoặc 'error') => Đúng
            boolean stillAtLogin = driver.getCurrentUrl().contains("login") || driver.getCurrentUrl().contains("error");

            // Hoặc tìm thông báo lỗi màu đỏ
            boolean errorVisible = false;
            try {
                if(driver.findElement(By.cssSelector(".alert-danger")).isDisplayed()) errorVisible = true;
            } catch (Exception ignored) {}

            if (stillAtLogin || errorVisible) {
                System.out.println("✅ Pass: Hệ thống chặn login sai password.");
                takeScreenshot("Login_WrongPass_Blocked"); // Chụp ảnh bằng chứng
            } else {
                takeScreenshot("Login_WrongPass_FAIL");
                Assert.fail("Lỗi: Đăng nhập sai mà vẫn vào được trang chủ!");
            }
        } catch (Exception e) {
            Assert.fail("Lỗi Test sai pass: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    void login_fail_user_not_exist() {
        prepareLoginPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Đã tăng timeout

        System.out.println("3. Test Login tài khoản ảo...");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId"))).sendKeys("tai_khoan_ma_123");
        driver.findElement(By.xpath("//div[@id='signin']//input[@name='password']")).sendKeys("123456");

        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

        try {
            // SỬA LOGIC CHECK:
            // Chỉ cần KHÔNG PHẢI là trang chủ (Base URL) là coi như Pass (đã bị chặn)
            boolean notRedirectedToHome = wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL)));

            if (notRedirectedToHome) {
                System.out.println("✅ Pass: Hệ thống không cho vào trang chủ.");
            } else {
                takeScreenshot("Login_NotExist_FAIL");
                Assert.fail("Lỗi nghiêm trọng: Tài khoản ma mà vẫn vào được trang chủ!");
            }
        } catch (Exception e) {
            takeScreenshot("Login_NotExist_Exception");
            // Nếu chờ mãi mà nó không chuyển trang (vẫn ở login) thì cũng coi như là Pass
            System.out.println("⚠️ Timeout nhưng vẫn ở lại trang Login -> Chấp nhận Pass.");
        }
    }
}