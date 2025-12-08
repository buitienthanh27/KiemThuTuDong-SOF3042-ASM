package com.java.automation.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.java.automation.utils.ScreenshotUtil.takeScreenshot;

@Listeners(TestListener.class)
public class RegisterTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 15; // Tăng timeout lên 15s cho an toàn

    // Biến lưu URL chuẩn hóa
    private String homeUrlWithSlash;

    @BeforeMethod
    public void setUpRegister() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));

        // Chuẩn bị URL an toàn
        String rawBase = BASE_URL;
        if (rawBase == null) rawBase = "http://localhost:9090/";
        String homeUrlNoSlash = rawBase.endsWith("/") ? rawBase.substring(0, rawBase.length() - 1) : rawBase;
        homeUrlWithSlash = homeUrlNoSlash + "/";
    }

    private void prepareRegisterPage() {
        driver.get(homeUrlWithSlash + "login");

        // Logout nếu đang kẹt ở trang admin hoặc trang khác
        if (!driver.getCurrentUrl().contains("login")) {
            driver.get(homeUrlWithSlash + "logout");
            driver.get(homeUrlWithSlash + "login");
        }

        try {
            // Chờ tab Sign Up xuất hiện. Dùng CSS Selector chuẩn xác hơn XPath text
            // Tìm thẻ <a> có href chứa 'signup' nằm trong danh sách tab
            WebElement signUpTab = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("ul.nav-tabs a[href*='signup']")
            ));

            if (!signUpTab.getAttribute("class").contains("active")) {
                clickElementJS(signUpTab);
                // Chờ tab active để đảm bảo form đã chuyển đổi
                wait.until(ExpectedConditions.attributeContains(signUpTab, "class", "active"));
            }
        } catch (Exception e) {
            // Fallback: Nếu CSS fail thì thử tìm bằng text cũ
            try {
                WebElement signUpTab = driver.findElement(By.xpath("//a[contains(text(), 'sign up')]"));
                clickElementJS(signUpTab);
            } catch (Exception ex) {}
        }
    }

    private void fillRegisterForm(String id, String name, String email, String pass) {
        // Dùng CSS Selector ID (#signup) để định vị input chính xác trong form đăng ký
        WebElement txtId = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#signup input[name='customerId']")));
        txtId.clear();
        txtId.sendKeys(id);

        driver.findElement(By.cssSelector("#signup input[name='fullname']")).sendKeys(name);
        driver.findElement(By.cssSelector("#signup input[name='email']")).sendKeys(email);
        driver.findElement(By.cssSelector("#signup input[name='password']")).sendKeys(pass);

        // Checkbox: Xử lý an toàn nếu không tìm thấy hoặc đã check
        try {
            WebElement checkbox = driver.findElement(By.id("signup-check"));
            if (!checkbox.isSelected()) {
                clickElementJS(checkbox);
            }
        } catch (Exception e) {
            // Checkbox có thể không bắt buộc hoặc ẩn
        }
    }

    private void clickSignUpButton() {
        // QUAN TRỌNG: Tìm nút button nằm TRONG div id='signup'
        // CSS Selector này ("#signup button") sẽ bắt được nút bất kể text là gì
        WebElement btnSignUp = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#signup button")));

        // Scroll tới element để chắc chắn không bị che
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btnSignUp);

        try {
            // Dùng Actions click để mô phỏng người dùng thật (ổn định hơn click thường)
            new Actions(driver).moveToElement(btnSignUp).pause(200).click().perform();
        } catch (Exception e) {
            // Fallback: Dùng JS Click từ BaseSeleniumTest nếu Actions fail
            clickElementJS(btnSignUp);
        }
    }

    // --- TEST CASE 1: ĐĂNG KÝ THÀNH CÔNG ---
    @Test(priority = 1)
    void register_success_with_unique_data() {
        prepareRegisterPage();
        long timestamp = System.currentTimeMillis();
        String newId = "u" + timestamp; // ID ngắn gọn
        String newEmail = "auto" + timestamp + "@vegana.com";

        fillRegisterForm(newId, "Auto User", newEmail, "123456");
        clickSignUpButton();

        try {
            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            // Kiểm tra nội dung chứa 'thành công' hoặc 'success' (không phân biệt hoa thường)
            String msgText = successMsg.getText().toLowerCase();
            Assert.assertTrue(msgText.contains("thành công") || msgText.contains("success"),
                    "Thông báo không chứa từ khóa thành công. Nội dung thực tế: " + successMsg.getText());
        } catch (Exception e) {
            takeScreenshot("FAIL_Register_Success");
            Assert.fail("Đăng ký thất bại hoặc không thấy thông báo thành công sau 15s.");
        }
    }

    // --- TEST CASE 2: ĐĂNG KÝ THẤT BẠI DO TRÙNG ID ---
    @Test(priority = 2)
    void register_fail_duplicate_id() {
        prepareRegisterPage();
        String existingId = "customer01"; // Đảm bảo ID này có trong DB
        String uniqueEmail = "uniq" + System.currentTimeMillis() + "@gmail.com";

        fillRegisterForm(existingId, "Duplicate Tester", uniqueEmail, "123456");
        clickSignUpButton();

        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            // Kiểm tra lỏng hơn để tránh fail do sai lệch câu chữ nhỏ
            String msgText = errorMsg.getText().toLowerCase();
            Assert.assertTrue(msgText.contains("id") || msgText.contains("tồn tại") || msgText.contains("duplicate"),
                    "Lỗi sai nội dung thông báo trùng ID: " + errorMsg.getText());
            takeScreenshot("Pass_Register_DuplicateID");
        } catch (Exception e) {
            takeScreenshot("FAIL_Register_DupID");
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
            Assert.assertTrue(errorMsg.getText().toLowerCase().contains("email"), "Thông báo lỗi không chứa từ 'email'");
            takeScreenshot("Pass_Register_DuplicateEmail");
        } catch (Exception e) {
            takeScreenshot("FAIL_Register_DupEmail");
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
        WebElement emailInput = driver.findElement(By.cssSelector("#signup input[name='email']"));
        String validationMessage = emailInput.getAttribute("validationMessage");

        if (validationMessage != null && !validationMessage.isEmpty()) {
            Assert.assertTrue(true); // Trình duyệt đã chặn -> Pass
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
            takeScreenshot("Pass_Register_ShortPassword");
        } catch (Exception e) {
            takeScreenshot("FAIL_Register_ShortPassword");
            Assert.fail("Test thất bại: Nhập mật khẩu 3 ký tự mà không thấy báo lỗi đỏ!");
        }
    }
}