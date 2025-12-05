package com.java.automation.selenium;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(ScreenshotOnFailureExtension.class)
public class RegisterTest extends BaseSeleniumTest {

    private static final int TIMEOUT = 15;

    /**
     * HÀM CHỤP ẢNH THỦ CÔNG
     */
    public void takeScreenshot(String fileName, String fail) {
        try {
            // 1. QUAN TRỌNG: Cuộn lên đầu trang trước tiên
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            Thread.sleep(500); // Chờ cuộn xong

            // 2. Chụp ảnh dưới dạng Byte (Để đính kèm vào Allure Report)
            byte[] content = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(fileName, new ByteArrayInputStream(content));

            // 3. Lưu ảnh ra File (Để xem offline hoặc lưu vào Artifacts của Github)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFileName = "screenshots/ERROR_" + fileName + "_" + timestamp + ".png";

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            java.nio.file.Path destination = java.nio.file.Paths.get(fullFileName);
            java.nio.file.Files.createDirectories(destination.getParent());
            java.nio.file.Files.copy(scrFile.toPath(), destination);

            System.out.println("📸 Đã chụp ảnh và đính kèm vào Allure Report: " + fullFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareRegisterPage() {
        driver.get("http://localhost:9090/login");
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        if (driver.getCurrentUrl().contains("admin")) {
            driver.get("http://localhost:9090/logout");
            driver.get("http://localhost:9090/login");
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
    @Test
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
            Assertions.assertTrue(successMsg.getText().contains("thành công"), "Không thấy chữ 'thành công'");


        } catch (Exception e) {
            Assertions.fail("Đăng ký thất bại.");
        }
    }

    // --- TEST CASE 2: ĐĂNG KÝ THẤT BẠI DO TRÙNG ID ---
    @Test
    void register_fail_duplicate_id() {
        prepareRegisterPage();
        String existingId = "customer01";
        String uniqueEmail = "newmail" + System.currentTimeMillis() + "@gmail.com";

        fillRegisterForm(existingId, "Duplicate Tester", uniqueEmail, "123456");

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            Assertions.assertTrue(errorMsg.getText().contains("ID Login này đã được sử dụng"), "Lỗi sai nội dung");

            // --- CHỤP ẢNH KHI PASS ---
            takeScreenshot("FAIL_Register_DuplicateID", "FAIL");

        } catch (Exception e) {
            Assertions.fail("Test thất bại: Không báo lỗi trùng ID!");
        }
    }

    // --- TEST CASE 3: ĐĂNG KÝ THẤT BẠI DO TRÙNG EMAIL ---
    @Test
    void register_fail_duplicate_email() {
        prepareRegisterPage();
        String uniqueId = "newuser" + System.currentTimeMillis();
        String existingEmail = "customer01@gmail.com";

        fillRegisterForm(uniqueId, "Duplicate Email Tester", existingEmail, "123456");

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("email"), "Lỗi sai nội dung");

            // --- CHỤP ẢNH KHI PASS ---
            takeScreenshot("FAIL_Register_DuplicateEmail", "Fail");
        } catch (Exception e) {
            Assertions.fail("Test thất bại: Không báo lỗi trùng Email!");
        }
    }

    // --- TEST CASE 4: ĐĂNG KÝ THẤT BẠI DO EMAIL SAI ĐỊNH DẠNG (Thiếu @, .com...) ---
    @Test
    void register_fail_invalid_email_format() {
        prepareRegisterPage();

        String uniqueId = "user" + System.currentTimeMillis();
        // Email sai định dạng (thiếu @)
        String invalidEmail = "nguyenvana_gmail.com";

        System.out.println("Đang test Email sai định dạng: " + invalidEmail);

        fillRegisterForm(uniqueId, "Invalid Email Tester", invalidEmail, "123456");

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        // Kỹ thuật kiểm tra HTML5 Validation (Vì Chrome sẽ chặn không cho submit)
        // Ta sẽ kiểm tra xem ô Email có đang bị trình duyệt báo lỗi không
        WebElement emailInput = driver.findElement(By.xpath("//div[@id='signup']//input[@name='email']"));

        // Lấy tin nhắn lỗi của trình duyệt (Ví dụ: "Please include an '@' in the email address...")
        String validationMessage = emailInput.getAttribute("validationMessage");

        System.out.println("Thông báo của trình duyệt: " + validationMessage);

        // Nếu validationMessage không rỗng => Trình duyệt đã chặn thành công -> PASS
        if (!validationMessage.isEmpty()) {
            Assertions.assertTrue(true); // Pass
            takeScreenshot("FAIL_Register_InvalidEmail_BrowserBlocked", "FAIL");
        } else {
            // Trường hợp trình duyệt không chặn (hiếm), ta kiểm tra Server có báo lỗi đỏ không
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            try {
                WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
                takeScreenshot("FAIL_Register_InvalidEmail_ServerBlocked", "FAIL");
            } catch (Exception e) {
                Assertions.fail("Thất bại: Nhập email sai định dạng mà hệ thống không báo lỗi gì cả!");
            }
        }
    }

    // --- TEST CASE 5: ĐĂNG KÝ THẤT BẠI DO MẬT KHẨU QUÁ NGẮN (< 6 ký tự) ---
    @Test
    void register_fail_short_password() {
        prepareRegisterPage();

        String uniqueId = "user" + System.currentTimeMillis();
        String validEmail = uniqueId + "@test.com";
        String shortPass = "123"; // Mật khẩu 3 ký tự

        System.out.println("Đang test mật khẩu ngắn: " + shortPass);

        fillRegisterForm(uniqueId, "Short Pass Tester", validEmail, shortPass);

        WebElement btnSignUp = driver.findElement(By.xpath("//button[contains(text(), 'sign up free')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        // Kiểm tra lỗi
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // Tìm thông báo lỗi màu đỏ
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            System.out.println("Lỗi nhận được: " + errorMsg.getText());

            // Kiểm tra nội dung lỗi (Tùy thuộc vào Backend của bạn trả về message gì)
            // Thường là "Password must be..." hoặc "Mật khẩu phải..." hoặc lỗi chung chung
            boolean isErrorCorrect = errorMsg.getText().toLowerCase().contains("password") ||
                    errorMsg.getText().toLowerCase().contains("mật khẩu") ||
                    errorMsg.getText().toLowerCase().contains("ngắn") ||
                    errorMsg.getText().toLowerCase().contains("failed"); // Dự phòng lỗi chung

            Assertions.assertTrue(isErrorCorrect, "Thông báo lỗi không nhắc gì đến mật khẩu");

            takeScreenshot("FAIL_Register_ShortPassword", "FAIL");

        } catch (Exception e) {
            Assertions.fail("Test thất bại: Nhập mật khẩu 3 ký tự mà không thấy báo lỗi đỏ!");
        }
    }
}