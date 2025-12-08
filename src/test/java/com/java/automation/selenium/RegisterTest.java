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
    private static final int TIMEOUT = 15;
    private String homeUrlWithSlash;

    @BeforeMethod
    public void setUpRegister() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));

        String rawBase = BASE_URL;
        if (rawBase == null) rawBase = "http://localhost:9090/";
        String homeUrlNoSlash = rawBase.endsWith("/") ? rawBase.substring(0, rawBase.length() - 1) : rawBase;
        homeUrlWithSlash = homeUrlNoSlash + "/";
    }

    // Hàm click JS chuyên biệt, click bất chấp bị che
    private void forceClick(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            // Nếu vẫn fail thì thử scroll rồi click lại
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            try { Thread.sleep(500); } catch (InterruptedException ex) {}
            element.click();
        }
    }

    private void prepareRegisterPage() {
        driver.get(homeUrlWithSlash + "login");

        if (!driver.getCurrentUrl().contains("login")) {
            driver.get(homeUrlWithSlash + "logout");
            driver.get(homeUrlWithSlash + "login");
        }

        try {
            // Tìm tab bằng CSS Selector chính xác
            WebElement signUpTab = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("ul.nav-tabs a[href*='signup'], ul.nav-tabs a[href*='register']")
            ));

            if (!signUpTab.getAttribute("class").contains("active")) {
                forceClick(signUpTab); // Dùng force click
                wait.until(ExpectedConditions.attributeContains(signUpTab, "class", "active"));
            }
        } catch (Exception e) {
            try {
                WebElement signUpTab = driver.findElement(By.xpath("//a[contains(text(), 'sign up') or contains(text(), 'Đăng ký')]"));
                forceClick(signUpTab);
            } catch (Exception ex) {}
        }
    }

    private void fillRegisterForm(String id, String name, String email, String pass) {
        // Chờ textbox xuất hiện
        WebElement txtId = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#signup input[name='customerId']")));
        txtId.clear();
        txtId.sendKeys(id);

        driver.findElement(By.cssSelector("#signup input[name='fullname']")).sendKeys(name);
        driver.findElement(By.cssSelector("#signup input[name='email']")).sendKeys(email);
        driver.findElement(By.cssSelector("#signup input[name='password']")).sendKeys(pass);

        try {
            WebElement checkbox = driver.findElement(By.id("signup-check"));
            if (!checkbox.isSelected()) {
                forceClick(checkbox);
            }
        } catch (Exception ignored) {}
    }

    private void clickSignUpButton() {
        // Tìm nút button
        WebElement btnSignUp = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#signup button")));

        // FIX QUAN TRỌNG: Dùng JS Click trực tiếp thay vì Actions
        // Điều này giúp click xuyên qua mọi layer (header/footer) nếu bị che
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSignUp);

        // Chờ 1 chút để server xử lý sau khi click
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    // --- TEST CASE 1: ĐĂNG KÝ THÀNH CÔNG ---
    @Test(priority = 1)
    void register_success_with_unique_data() {
        prepareRegisterPage();
        long timestamp = System.currentTimeMillis();
        String newId = "u" + timestamp;
        String newEmail = "auto" + timestamp + "@vegana.com";

        fillRegisterForm(newId, "Auto User", newEmail, "123456");
        clickSignUpButton();

        try {
            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            String msgText = successMsg.getText().toLowerCase();
            Assert.assertTrue(msgText.contains("thành công") || msgText.contains("success") || msgText.contains("created"),
                    "Thông báo không đúng: " + successMsg.getText());
        } catch (Exception e) {
            takeScreenshot("FAIL_Register_Success");
            Assert.fail("Đăng ký thất bại: Không thấy thông báo thành công sau 15s.");
        }
    }

    // --- TEST CASE 2: TRÙNG ID ---
    @Test(priority = 2)
    void register_fail_duplicate_id() {
        prepareRegisterPage();
        String existingId = "customer01";

        fillRegisterForm(existingId, "Dup User", "unique" + System.currentTimeMillis() + "@gmail.com", "123456");
        clickSignUpButton();

        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            String msgText = errorMsg.getText().toLowerCase();
            boolean isIdError = msgText.contains("id") || msgText.contains("tồn tại") || msgText.contains("duplicate") || msgText.contains("đã có");
            Assert.assertTrue(isIdError, "Lỗi không báo trùng ID: " + errorMsg.getText());
            takeScreenshot("Pass_Register_DuplicateID");
        } catch (Exception e) {
            takeScreenshot("FAIL_Register_DupID");
            Assert.fail("Test thất bại: Không báo lỗi trùng ID (Alert đỏ không xuất hiện)!");
        }
    }

    // --- TEST CASE 3: TRÙNG EMAIL ---
    @Test(priority = 3)
    void register_fail_duplicate_email() {
        prepareRegisterPage();
        String uniqueId = "new" + System.currentTimeMillis();
        String existingEmail = "admin@vegana.com";

        fillRegisterForm(uniqueId, "Dup Email", existingEmail, "123456");
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

    // --- TEST CASE 4: EMAIL SAI ĐỊNH DẠNG ---
    @Test(priority = 4)
    void register_fail_invalid_email_format() {
        prepareRegisterPage();
        String uniqueId = "user" + System.currentTimeMillis();

        fillRegisterForm(uniqueId, "Invalid Email", "nguyenvana_gmail.com", "123456");
        clickSignUpButton();

        // 1. Check HTML5 Validation
        WebElement emailInput = driver.findElement(By.cssSelector("#signup input[name='email']"));
        String validationMessage = emailInput.getAttribute("validationMessage");

        if (validationMessage != null && !validationMessage.isEmpty()) {
            Assert.assertTrue(true);
        } else {
            // 2. Check Server Validation
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
                takeScreenshot("Pass_Register_InvalidEmail_Server");
            } catch (Exception e) {
                takeScreenshot("FAIL_Register_InvalidEmail");
                Assert.fail("Thất bại: Email sai định dạng mà không báo lỗi!");
            }
        }
    }

    // --- TEST CASE 5: PASS NGẮN ---
    @Test(priority = 5)
    void register_fail_short_password() {
        prepareRegisterPage();
        String uniqueId = "user" + System.currentTimeMillis();

        fillRegisterForm(uniqueId, "Short Pass", uniqueId + "@test.com", "123");
        clickSignUpButton();

        try {
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
            takeScreenshot("Pass_Register_ShortPassword");
        } catch (Exception e) {
            takeScreenshot("FAIL_Register_ShortPassword");
            Assert.fail("Test thất bại: Mật khẩu 3 ký tự mà không báo lỗi!");
        }
    }
}