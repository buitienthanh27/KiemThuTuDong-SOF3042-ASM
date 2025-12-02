package com.java.automation.pages;

import com.java.automation.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object Model for Login/Register page
 */
public class LoginOrRegisterPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Login Tab
    @FindBy(xpath = "//a[contains(@href, '#signin')]")
    private WebElement signInTab;

    // Login Form Elements
    @FindBy(name = "customerId")
    private WebElement loginCustomerIdInput;

    @FindBy(name = "password")
    private WebElement loginPasswordInput;

    @FindBy(xpath = "//form[@action='/doLogin']//button[@type='submit']")
    private WebElement signInButton;

    // Register Tab
    @FindBy(xpath = "//a[contains(@href, '#signup')]")
    private WebElement signUpTab;

    // Register Form Elements
    @FindBy(xpath = "//form[@action='/registered']//input[@placeholder='ID Login']")
    private WebElement registerCustomerIdInput;

    @FindBy(xpath = "//form[@action='/registered']//input[@placeholder='Full Name']")
    private WebElement registerFullnameInput;

    @FindBy(xpath = "//form[@action='/registered']//input[@type='email']")
    private WebElement registerEmailInput;

    @FindBy(xpath = "//form[@action='/registered']//input[@type='password']")
    private WebElement registerPasswordInput;

    @FindBy(xpath = "//form[@action='/registered']//button[@type='submit']")
    private WebElement signUpButton;

    // Alert Messages
    @FindBy(xpath = "//div[contains(@class, 'alert-danger')]")
    private WebElement errorAlert;

    @FindBy(xpath = "//div[contains(@class, 'alert-success')]")
    private WebElement successAlert;

    // Remember me checkbox
    @FindBy(id = "signin-check")
    private WebElement rememberMeCheckbox;

    // Forgot password link
    @FindBy(xpath = "//a[contains(text(), 'Forgot password')]")
    private WebElement forgotPasswordLink;

    public LoginOrRegisterPage(WebDriver driver) {
        this.driver = driver;
        // Selenium 4.x sử dụng Duration thay vì long (seconds)
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    // Navigation methods
    public void navigateToLoginPage() {
        String baseUrl = TestConfig.getBaseUrl();
        // Đảm bảo baseUrl không kết thúc bằng "/" để tránh "//"
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOf(signInTab));
    }

    public void clickSignInTab() {
        wait.until(ExpectedConditions.elementToBeClickable(signInTab));
        signInTab.click();
        // Wait for sign in form to be visible
        wait.until(ExpectedConditions.visibilityOf(loginCustomerIdInput));
    }

    public void clickSignUpTab() {
        wait.until(ExpectedConditions.elementToBeClickable(signUpTab));
        signUpTab.click();
        // Wait for sign up form to be visible
        wait.until(ExpectedConditions.visibilityOf(registerCustomerIdInput));
    }

    // Login methods
    public void enterLoginCustomerId(String customerId) {
        wait.until(ExpectedConditions.visibilityOf(loginCustomerIdInput));
        loginCustomerIdInput.clear();
        loginCustomerIdInput.sendKeys(customerId);
    }

    public void enterLoginPassword(String password) {
        wait.until(ExpectedConditions.visibilityOf(loginPasswordInput));
        loginPasswordInput.clear();
        loginPasswordInput.sendKeys(password);
    }

    public void clickSignInButton() {
        wait.until(ExpectedConditions.elementToBeClickable(signInButton));
        signInButton.click();
        // Wait for page to process - either redirect to home or show error
        try {
            // Wait a moment for page to process
            Thread.sleep(500);
            // Check if error alert appears (stays on login page)
            try {
                wait.until(ExpectedConditions.visibilityOf(errorAlert));
            } catch (Exception e) {
                // No error alert, might have redirected - that's OK
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void login(String customerId, String password) {
        clickSignInTab();
        enterLoginCustomerId(customerId);
        enterLoginPassword(password);
        clickSignInButton();
    }

    public void checkRememberMe() {
        if (!rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
    }

    // Registration methods
    public void enterRegisterCustomerId(String customerId) {
        wait.until(ExpectedConditions.visibilityOf(registerCustomerIdInput));
        registerCustomerIdInput.clear();
        registerCustomerIdInput.sendKeys(customerId);
    }

    public void enterRegisterFullname(String fullname) {
        wait.until(ExpectedConditions.visibilityOf(registerFullnameInput));
        registerFullnameInput.clear();
        registerFullnameInput.sendKeys(fullname);
    }

    public void enterRegisterEmail(String email) {
        wait.until(ExpectedConditions.visibilityOf(registerEmailInput));
        registerEmailInput.clear();
        registerEmailInput.sendKeys(email);
    }

    public void enterRegisterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOf(registerPasswordInput));
        registerPasswordInput.clear();
        registerPasswordInput.sendKeys(password);
    }

    public void clickSignUpButton() {
        wait.until(ExpectedConditions.elementToBeClickable(signUpButton));
        signUpButton.click();
        // Wait for page to process - either show success/error alert or stay on page
        try {
            // Wait a moment for page to process
            Thread.sleep(500);
            // Check if success or error alert appears
            try {
                wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOf(successAlert),
                    ExpectedConditions.visibilityOf(errorAlert)
                ));
            } catch (Exception e) {
                // No alert, validation might have prevented submission - that's OK
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void register(String customerId, String fullname, String email, String password) {
        clickSignUpTab();
        enterRegisterCustomerId(customerId);
        enterRegisterFullname(fullname);
        enterRegisterEmail(email);
        enterRegisterPassword(password);
        clickSignUpButton();
    }

    // Validation methods
    public boolean isErrorAlertDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorAlert));
            return errorAlert.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuccessAlertDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successAlert));
            return successAlert.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorAlertText() {
        if (isErrorAlertDisplayed()) {
            return errorAlert.getText();
        }
        return "";
    }

    public String getSuccessAlertText() {
        if (isSuccessAlertDisplayed()) {
            return successAlert.getText();
        }
        return "";
    }

    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains("/login");
    }

    public boolean isOnHomePage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.equals(TestConfig.getBaseUrl() + "/") || 
               currentUrl.equals(TestConfig.getBaseUrl() + "/?login_success");
    }
}

