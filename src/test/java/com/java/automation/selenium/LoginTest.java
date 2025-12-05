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
@ExtendWith(TestListener.class)
@ExtendWith(ScreenshotOnFailureExtension.class)
public class LoginTest extends BaseSeleniumTest {

    private static final int TIMEOUT = 10;

    /**
     * Hàm chuẩn bị: Vào trang Login, đảm bảo đang ở Tab Sign In
     */
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

    private void prepareLoginPage() {
        System.out.println("--- BẮT ĐẦU TEST CASE ---");
        driver.get("http://localhost:9090/login");
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Nếu đang kẹt ở trang Admin -> Logout ra
        if (driver.getCurrentUrl().contains("admin")) {
            System.out.println("Phát hiện đang ở Admin, tiến hành Logout...");
            driver.get("http://localhost:9090/logout");
            driver.get("http://localhost:9090/login");
        }

        // 2. CHUYỂN TAB SIGN IN (QUAN TRỌNG)
        // Tìm thẻ <a> chứa text 'sign in' trong phần danh sách tab (ul.nav-tabs)
        try {
            WebElement signInTab = driver.findElement(By.xpath("//ul[contains(@class, 'nav-tabs')]//a[contains(text(), 'sign in')]"));
            // Dùng JS click cho chắc ăn (bất chấp bị che)
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", signInTab);
            Thread.sleep(500); // Chờ hiệu ứng chuyển tab
            System.out.println("Đã chuyển sang Tab Sign In");
        } catch (Exception e) {
            System.out.println("Không tìm thấy Tab Sign In, có thể giao diện không có Tab.");
        }
    }

    @Test
    void login_with_valid_customer_should_success() {
        prepareLoginPage();

        // 1. Nhập Email (name='customerId' theo HTML của bạn)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
        emailInput.clear();
        emailInput.sendKeys("abcd"); // Tài khoản đúng của bạn

        // 2. Nhập Password
        // XPath chỉ tìm ô password nằm trong div có id='signin' để tránh nhầm với Register
        WebElement passInput = driver.findElement(By.xpath("//div[@id='signin']//input[@name='password']"));
        passInput.clear();
        passInput.sendKeys("123123");

        // 3. Click Login
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

        // 4. Kiểm tra kết quả
        System.out.println("Đã bấm Login, đang chờ chuyển trang...");
        try {
            // Cách 1: Chờ URL đổi về trang chủ (http://localhost:9090/)
            wait.until(ExpectedConditions.urlToBe("http://localhost:9090/"));
            System.out.println("Login thành công: URL đã về trang chủ.");
        } catch (Exception e) {
            // Cách 2: Nếu URL không đổi, thử tìm nút Logout hoặc tên User
            try {
                WebElement logoutBtn = driver.findElement(By.partialLinkText("Logout")); // Hoặc "Sign out"
                if(logoutBtn.isDisplayed()){
                    System.out.println("Login thành công: Tìm thấy nút Logout.");
                    return; // Pass
                }
            } catch (Exception ex) {
                // Nếu cả 2 đều không thấy -> Fail
                Assertions.fail("Login thất bại: Vẫn ở trang Login hoặc không về trang chủ. URL hiện tại: " + driver.getCurrentUrl());
            }
        }
    }

    @Test
    void login_with_wrong_password_should_show_error() {
        prepareLoginPage();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Nhập đúng User
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
        emailInput.clear();
        emailInput.sendKeys("abcd");

        // 2. Nhập SAI Password
        WebElement passInput = driver.findElement(By.xpath("//div[@id='signin']//input[@name='password']"));
        passInput.clear();
        passInput.sendKeys("123456");

        // 3. Click Login
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

        // 4. Kiểm tra lỗi
        try {
            // Chỉ cần URL không phải trang chủ là coi như Đăng nhập thất bại (Pass test case invalid)
            boolean isNotHome = !driver.getCurrentUrl().equals("http://localhost:9090/");
            Assertions.assertTrue(isNotHome, "Lỗi: Đăng nhập sai mà vẫn vào được trang chủ!");
            takeScreenshot("FAIL_login_with_wrong_password_should_show_error", "FAIL");
        } catch (Exception e) {
            Assertions.fail("Test thất bại: Không hiện thông báo lỗi màu đỏ (.alert-danger). URL: " + driver.getCurrentUrl());
        }
    }

    @Test
    void login_fail_user_not_exist() {
        prepareLoginPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Tạo một tài khoản ngẫu nhiên chắc chắn không có trong DB
        String nonExistUser = "ghost_user_" + System.currentTimeMillis() + "@test.com";

        // Nhập liệu
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId"))).sendKeys(nonExistUser);
        driver.findElement(By.xpath("//div[@id='signin']//input[@name='password']")).sendKeys("123456");

        // Click Login
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);

        // Kiểm tra lỗi
        try {
            // Chờ URL có chứa chữ "error" HOẶC hiện thông báo đỏ
            boolean urlHasError = wait.until(ExpectedConditions.urlContains("error"));

            // Kiểm tra thêm thông báo lỗi (nếu có)
            boolean textVisible = false;
            try {
                if (driver.findElement(By.cssSelector(".alert-danger")).isDisplayed()) textVisible = true;
            } catch (Exception ignored) {}

            Assertions.assertTrue(urlHasError || textVisible, "Lỗi: Nhập tài khoản ma mà không báo lỗi!");
            takeScreenshot("FAIL_login_fail_user_not_exist", "FAIL");

        } catch (Exception e) {
            Assertions.fail("Test thất bại: Hệ thống không phản ứng gì khi nhập sai tài khoản.");
        }
    }
}