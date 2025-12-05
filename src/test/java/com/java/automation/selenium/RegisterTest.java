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
public class RegisterTest extends BaseSeleniumTest {

    private static final int TIMEOUT = 15;

    private void prepareRegisterPage() {
        driver.get(BASE_URL + "login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        if (driver.getCurrentUrl().contains("admin")) {
            driver.get(BASE_URL + "logout");
            driver.get(BASE_URL + "login");
        }

        try {
            WebElement signUpTab = driver.findElement(By.xpath("//a[contains(text(), 'sign up') and @data-toggle='tab']"));
            if (!signUpTab.getAttribute("class").contains("active")) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", signUpTab);
                Thread.sleep(1000);
            }
        } catch (Exception ignored) { }
    }

    private void fillRegisterForm(String id, String name, String email, String pass) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement txtId = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='signup']//input[@name='customerId']")));
        txtId.clear();
        txtId.sendKeys(id);
        driver.findElement(By.xpath("//div[@id='signup']//input[@name='fullname']")).sendKeys(name);
        driver.findElement(By.xpath("//div[@id='signup']//input[@name='email']")).sendKeys(email);
        driver.findElement(By.xpath("//div[@id='signup']//input[@name='password']")).sendKeys(pass);

        WebElement checkbox = driver.findElement(By.id("signup-check"));
        if (!checkbox.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
        }
    }

    // --- TEST CASE 1: ĐĂNG KÝ THÀNH CÔNG ---
    @Test(priority = 1)
    void register_success_with_unique_data() {
        prepareRegisterPage();
        long timestamp = System.currentTimeMillis();
        String newId = "user" + timestamp;
        String newEmail = "test" + timestamp + "@vegana.com";

        fillRegisterForm(newId, "Test User Auto", newEmail, "123456");

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            Assert.assertTrue(successMsg.getText().contains("thành công"), "Không thấy chữ 'thành công'");
        } catch (Exception e) {
            Assert.fail("Đăng ký thất bại.");
        }
    }

    // --- TEST CASE 2: ĐĂNG KÝ THẤT BẠI DO TRÙNG ID ---
    @Test(priority = 2)
    void register_fail_duplicate_id() {
        prepareRegisterPage();
        String existingId = "customer01"; // Đảm bảo ID này có trong DB (do file data.sql tạo)
        String uniqueEmail = "newmail" + System.currentTimeMillis() + "@gmail.com";

        fillRegisterForm(existingId, "Duplicate Tester", uniqueEmail, "123456");

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            Assert.assertTrue(errorMsg.getText().contains("ID Login này đã được sử dụng"), "Lỗi sai nội dung");
            takeScreenshot("FAIL_Register_DuplicateID");
        } catch (Exception e) {
            Assert.fail("Test thất bại: Không báo lỗi trùng ID!");
        }
    }

    // --- TEST CASE 3: ĐĂNG KÝ THẤT BẠI DO TRÙNG EMAIL ---
    @Test(priority = 3)
    void register_fail_duplicate_email() {
        prepareRegisterPage();
        String uniqueId = "newuser" + System.currentTimeMillis();
        String existingEmail = "admin@vegana.com"; // Email admin có sẵn

        fillRegisterForm(uniqueId, "Duplicate Email Tester", existingEmail, "123456");

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            Assert.assertTrue(errorMsg.getText().toLowerCase().contains("email"), "Lỗi sai nội dung");
            takeScreenshot("FAIL_Register_DuplicateEmail");
        } catch (Exception e) {
            Assert.fail("Test thất bại: Không báo lỗi trùng Email!");
        }
    }

    // --- TEST CASE 4: ĐĂNG KÝ THẤT BẠI DO EMAIL SAI ĐỊNH DẠNG ---
    @Test(priority = 4)
    void register_fail_invalid_email_format() {
        prepareRegisterPage();
        String uniqueId = "user" + System.currentTimeMillis();
        String invalidEmail = "nguyenvana_gmail.com";

        fillRegisterForm(uniqueId, "Invalid Email Tester", invalidEmail, "123456");

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        // Kiểm tra HTML5 Validation
        WebElement emailInput = driver.findElement(By.xpath("//div[@id='signup']//input[@name='email']"));
        String validationMessage = emailInput.getAttribute("validationMessage");

        if (!validationMessage.isEmpty()) {
            Assert.assertTrue(true); // Pass
            System.out.println("Pass: Trình duyệt chặn email sai.");
            takeScreenshot("FAIL_Register_InvalidEmail_BrowserBlocked");
        } else {
            // Kiểm tra Server Validation
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            try {
                WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
                takeScreenshot("FAIL_Register_InvalidEmail_ServerBlocked");
            } catch (Exception e) {
                Assert.fail("Thất bại: Nhập email sai định dạng mà hệ thống không báo lỗi gì cả!");
            }
        }
    }

    // --- TEST CASE 5: MẬT KHẨU NGẮN ---
    @Test(priority = 5)
    void register_fail_short_password() {
        prepareRegisterPage();
        String uniqueId = "user" + System.currentTimeMillis();
        String validEmail = uniqueId + "@test.com";
        String shortPass = "123";

        fillRegisterForm(uniqueId, "Short Pass Tester", validEmail, shortPass);

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            boolean isErrorCorrect = errorMsg.getText().toLowerCase().contains("password") ||
                    errorMsg.getText().toLowerCase().contains("mật khẩu") ||
                    errorMsg.getText().toLowerCase().contains("ngắn") ||
                    errorMsg.getText().toLowerCase().contains("failed");

            Assert.assertTrue(isErrorCorrect, "Thông báo lỗi không nhắc gì đến mật khẩu");
            takeScreenshot("FAIL_Register_ShortPassword");

        } catch (Exception e) {
            Assert.fail("Test thất bại: Nhập mật khẩu 3 ký tự mà không thấy báo lỗi đỏ!");
        }
    }
}