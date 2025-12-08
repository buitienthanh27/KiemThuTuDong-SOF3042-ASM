package com.java.automation.pages;

import com.java.automation.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginOrRegisterPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // --- LOCATORS: LOGIN ---
    private By signInTab = By.xpath("//ul[contains(@class, 'nav-tabs')]//a[contains(text(), 'sign in')]");
    private By loginIdInput = By.name("customerId");
    private By loginPassInput = By.name("password");
    private By signInButton = By.xpath("//div[@id='signin']//button[contains(text(), 'sign in now')]");

    // --- LOCATORS: REGISTER ---
    private By signUpTab = By.xpath("//ul[contains(@class, 'nav-tabs')]//a[contains(text(), 'sign up')]");
    private By regIdInput = By.xpath("//div[@id='signup']//input[@name='customerId']");
    private By regNameInput = By.xpath("//div[@id='signup']//input[@name='fullname']");
    private By regEmailInput = By.xpath("//div[@id='signup']//input[@name='email']");
    private By regPassInput = By.xpath("//div[@id='signup']//input[@name='password']");
    private By signUpButton = By.xpath("//div[@id='signup']//button[contains(text(), 'sign up now')]");

    // --- LOCATORS: COMMON ---
    private By logoutButton = By.partialLinkText("Logout");
    private By errorAlert = By.cssSelector(".alert-danger");   // Thông báo lỗi đỏ
    private By successAlert = By.cssSelector(".alert-success"); // Thông báo thành công xanh

    // Constructor
    public LoginOrRegisterPage(WebDriver driver) {
        this.driver = driver;
        // Giữ wait mặc định 30s cho các thao tác tương tác element (click, sendKeys...)
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // ===========================
    // ACTIONS: NAVIGATION
    // ===========================
    public void navigateToLoginPage() {
        String baseUrl = TestConfig.getProperty("base.url");
        if (baseUrl == null) baseUrl = "http://localhost:9090/";
        if (!baseUrl.endsWith("/")) baseUrl += "/";

        driver.get(baseUrl + "login");

        // Logout nếu đang kẹt phiên cũ
        if (!driver.getCurrentUrl().contains("login")) {
            driver.get(baseUrl + "logout");
            driver.get(baseUrl + "login");
        }
    }

    // ===========================
    // ACTIONS: LOGIN
    // ===========================
    public void clickSignInTab() {
        clickElement(signInTab);
    }

    public void login(String username, String password) {
        clickSignInTab();

        // Wait until visible and then send keys immediately
        WebElement idInput = wait.until(ExpectedConditions.visibilityOfElementLocated(loginIdInput));
        idInput.clear();
        idInput.sendKeys(username);

        WebElement passInput = driver.findElement(loginPassInput);
        passInput.clear();
        passInput.sendKeys(password);

        clickElement(signInButton);
    }

    // ===========================
    // ACTIONS: REGISTER
    // ===========================
    public void clickSignUpTab() {
        clickElement(signUpTab);
    }

    public void register(String id, String fullname, String email, String password) {
        clickSignUpTab();

        enterRegisterId(id);
        enterRegisterFullname(fullname);
        enterRegisterEmail(email);
        enterRegisterPassword(password);

        clickSignUpButton();
    }

    public void enterRegisterId(String id) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(regIdInput));
        el.clear();
        el.sendKeys(id);
    }

    public void enterRegisterFullname(String name) {
        driver.findElement(regNameInput).sendKeys(name);
    }

    public void enterRegisterEmail(String email) {
        driver.findElement(regEmailInput).sendKeys(email);
    }

    public void enterRegisterPassword(String pass) {
        driver.findElement(regPassInput).sendKeys(pass);
    }

    public void clickSignUpButton() {
        clickElement(signUpButton);
    }

    // Hàm hỗ trợ click bằng JS (Chống lỗi element click intercepted)
    private void clickElement(By locator) {
        try {
            // Chỉ chờ element CLICKABLE (có thể click được) là click ngay
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            // Fallback: Thử click thường nếu JS fail
            driver.findElement(locator).click();
        }
    }

    // ===========================
    // VERIFICATIONS (KIỂM TRA)
    // ===========================

    public boolean isOnHomePage() {
        try {
            String baseUrl = TestConfig.getProperty("base.url");
            if (baseUrl == null) baseUrl = "http://localhost:9090";

            // Xử lý dấu '/'
            String urlNoSlash = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            String urlWithSlash = urlNoSlash + "/";

            // Lấy URL hiện tại
            String currentUrl = driver.getCurrentUrl();

            // 1. Check NHANH: Nếu URL đúng là Home -> return true luôn
            if (currentUrl.equals(urlNoSlash) || currentUrl.equals(urlWithSlash)) {
                return true;
            }

            // 2. Check NHANH (Quan trọng): Nếu URL chứa "login" -> Chắc chắn không phải Home -> return false luôn
            // Bước này giúp tránh việc code chạy xuống dưới tìm nút Logout và bị dính chờ 30s
            if (currentUrl.contains("login")) {
                return false;
            }

            // 3. Chỉ khi URL không rõ ràng mới tìm nút Logout
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(logoutButton)).isDisplayed();

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnLoginPage() {
        try {
            // Check nhanh bằng URL, tránh tìm element không cần thiết
            return driver.getCurrentUrl().contains("login");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorAlertDisplayed() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuccessAlertDisplayed() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(successAlert)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorAlertText() {
        if (isErrorAlertDisplayed()) {
            return driver.findElement(errorAlert).getText();
        }
        return "";
    }

    public String getSuccessAlertText() {
        if (isSuccessAlertDisplayed()) {
            return driver.findElement(successAlert).getText();
        }
        return "";
    }
}