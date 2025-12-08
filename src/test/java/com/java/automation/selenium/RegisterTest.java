package com.java.automation.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;

@Listeners(TestListener.class)
public class RegisterTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 10; // Giảm xuống 10s cho nhanh

    // Biến lưu URL chuẩn hóa
    private String homeUrlWithSlash;

    @BeforeMethod
    public void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));

        // Chuẩn bị URL an toàn
        String rawBase = BASE_URL;
        if (rawBase == null) rawBase = "http://localhost:9090/";
        String homeUrlNoSlash = rawBase.endsWith("/") ? rawBase.substring(0, rawBase.length() - 1) : rawBase;
        homeUrlWithSlash = homeUrlNoSlash + "/";
    }

    // --- HÀM HỖ TRỢ ---

    private void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            // Wait ngắn để element ổn định (thay thế Thread.sleep dài)
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    private void prepareRegisterPage() {
        driver.get(homeUrlWithSlash + "login");

        // Logout nếu đang kẹt ở trang admin
        if (driver.getCurrentUrl().contains("admin")) {
            driver.get(homeUrlWithSlash + "logout");
            driver.get(homeUrlWithSlash + "login");
        }

        try {
            // Chờ tab Sign Up xuất hiện và click
            WebElement signUpTab = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(), 'sign up') and @data-toggle='tab']")
            ));

            if (!signUpTab.getAttribute("class").contains("active")) {
                clickElementJS(signUpTab);
                // Chờ tab active thay vì sleep cứng
                wait.until(ExpectedConditions.attributeContains(signUpTab, "class", "active"));
            }
        } catch (Exception ignored) { }
    }

    private void fillRegisterForm(String id, String name, String email, String pass) {
        WebElement txtId = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='signup']//input[@name='customerId']")));
        txtId.clear();
        txtId.sendKeys(id);

        driver.findElement(By.xpath("//div[@id='signup']//input[@name='fullname']")).sendKeys(name);
        driver.findElement(By.xpath("//div[@id='signup']//input[@name='email']")).sendKeys(email);
        driver.findElement(By.xpath("//div[@id='signup']//input[@name='password']")).sendKeys(pass);

        WebElement checkbox = driver.findElement(By.id("signup-check"));
        if (!checkbox.isSelected()) {
            clickElementJS(checkbox);
        }
    }

    private void clickSignUpButton() {
        WebElement btnSignUp = driver.findElement(By.xpath("//div[@id='signup']//button[contains(text(), 'sign up')]"));
        clickElementJS(btnSignUp);
    }

    // --- TEST CASE 1: ĐĂNG KÝ THÀNH CÔNG ---
    @Test(priority = 1)
    void register_success_with_unique_data() {
        prepareRegisterPage();
        long timestamp = System.currentTimeMillis();
        String newId = "user" + timestamp;
        String newEmail = "test" + timestamp + "@vegana.com";

        fillRegisterForm(newId, "Test User Auto", newEmail, "123456");
        clickSignUpButton();

        try {
            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            Assert.assertTrue(successMsg.getText().contains("thành công"), "Không thấy chữ 'thành công'");
        } catch (Exception e) {
            takeScreenshot("FAIL_Register_Success");
            Assert.fail("Đăng ký thất bại hoặc không thấy thông báo thành công.");
        }
    }

    // --- TEST CASE 2: ĐĂNG KÝ THẤT BẠI DO TRÙNG ID ---
    @Test(priority = 2)
    void register_fail_duplicate_id() {
        prepareRegisterPage();
        String existingId = "customer01"; // Đảm bảo ID này có trong DB
        String uniqueEmail = "newmail" + System.currentTimeMillis() + "@gmail.com";

        fillRegisterForm(existingId, "Duplicate Tester", uniqueEmail, "123456");
        clickSignUpButton();

        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            Assert.assertTrue(errorMsg.getText().contains("ID Login này đã được sử dụng"), "Lỗi sai nội dung thông báo trùng ID");
            takeScreenshot("Pass_Register_DuplicateID");
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
        clickSignUpButton();

        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            // Check mềm dẻo hơn (chữ hoa/thường)
            Assert.assertTrue(errorMsg.getText().toLowerCase().contains("email"), "Thông báo lỗi không chứa từ 'email'");
            takeScreenshot("Pass_Register_DuplicateEmail");
        } catch (Exception e) {
            Assert.fail("Test thất bại: Không báo lỗi trùng Email!");
        }
    }

    // --- TEST CASE 4: ĐĂNG KÝ THẤT BẠI DO EMAIL SAI ĐỊNH DẠNG ---
    @Test(priority = 4)
    void register_fail_invalid_email_format() {
        prepareRegisterPage();
        String uniqueId = "user" + System.currentTimeMillis();
        String invalidEmail = "nguyenvana_gmail.com"; // Thiếu @

        fillRegisterForm(uniqueId, "Invalid Email Tester", invalidEmail, "123456");
        clickSignUpButton();

        // 1. Kiểm tra HTML5 Validation (Client side)
        WebElement emailInput = driver.findElement(By.xpath("//div[@id='signup']//input[@name='email']"));
        String validationMessage = emailInput.getAttribute("validationMessage");

        if (validationMessage != null && !validationMessage.isEmpty()) {
            Assert.assertTrue(true); // Trình duyệt đã chặn -> Pass
            System.out.println("Pass: Trình duyệt chặn email sai định dạng.");
        } else {
            // 2. Nếu trình duyệt cho qua, kiểm tra Server Validation (Server side)
            try {
                WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
                takeScreenshot("Pass_Register_InvalidEmail_Server");
            } catch (Exception e) {
                takeScreenshot("FAIL_Register_InvalidEmail");
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
        clickSignUpButton();

        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            String msg = errorMsg.getText().toLowerCase();

            boolean isErrorCorrect = msg.contains("password") ||
                    msg.contains("mật khẩu") ||
                    msg.contains("ngắn") ||
                    msg.contains("failed");

            Assert.assertTrue(isErrorCorrect, "Thông báo lỗi không nhắc gì đến mật khẩu. Nội dung thực tế: " + errorMsg.getText());
            takeScreenshot("Pass_Register_ShortPassword");

        } catch (Exception e) {
            takeScreenshot("FAIL_Register_ShortPassword");
            Assert.fail("Test thất bại: Nhập mật khẩu 3 ký tự mà không thấy báo lỗi đỏ!");
        }
    }
}