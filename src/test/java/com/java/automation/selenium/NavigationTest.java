package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
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
import java.util.List;

@Listeners(TestListener.class)
public class NavigationTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 10;

    // Biến lưu URL chuẩn hóa để so sánh
    private String homeUrlNoSlash;
    private String homeUrlWithSlash;

    @BeforeMethod
    void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));

        // Chuẩn bị sẵn 2 dạng URL để so sánh cho chính xác
        String rawBase = BASE_URL; // Lấy từ BaseSeleniumTest
        if (rawBase == null) rawBase = "http://localhost:9090/";

        homeUrlNoSlash = rawBase.endsWith("/") ? rawBase.substring(0, rawBase.length() - 1) : rawBase;
        homeUrlWithSlash = homeUrlNoSlash + "/";
    }

    // --- HÀM HỖ TRỢ ---
    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    // Helper để lấy URL an toàn (luôn có / ở cuối để nối chuỗi)
    private String getBaseUrlWithSlash() {
        return homeUrlWithSlash;
    }

    private void ensureLoggedIn() {
        driver.get(getBaseUrlWithSlash() + "login");
        try {
            // Check nhanh: Nếu không ở trang login thì thôi (đã login rồi hoặc url khác)
            if (!driver.getCurrentUrl().contains("login")) return;

            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            userField.sendKeys("abcd");
            driver.findElement(By.name("password")).sendKeys("123123");
            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            clickElementJS(loginBtn);

            // FIX: Chấp nhận cả 2 dạng URL khi login thành công
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe(homeUrlNoSlash),
                    ExpectedConditions.urlToBe(homeUrlWithSlash)
            ));
        } catch (Exception e) {
            System.out.println("Login info: " + e.getMessage());
        }
    }

    // --- TEST 1: LOGO ---
    @Test(priority = 1)
    void test_logo_redirects_to_home() {
        driver.get(getBaseUrlWithSlash() + "contact");
        try {
            WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='logo']/parent::a")));
            logo.click();

            // FIX: Chờ 1 trong 2 URL (có hoặc không có dấu /)
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe(homeUrlNoSlash),
                    ExpectedConditions.urlToBe(homeUrlWithSlash)
            ));

            String currentUrl = driver.getCurrentUrl();
            boolean isHome = currentUrl.equals(homeUrlNoSlash) || currentUrl.equals(homeUrlWithSlash);
            Assert.assertTrue(isHome, "Không quay về đúng trang chủ! Current: " + currentUrl);

        } catch (Exception e) {
            takeScreenshot("Logo_Error");
            Assert.fail("Lỗi Logo: " + e.getMessage());
        }
    }

    // --- TEST 2: MENU ALL PRODUCTS ---
    @Test(priority = 2)
    void test_menu_all_products() {
        driver.get(getBaseUrlWithSlash());
        try {
            WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Products")));
            productLink.click();
            wait.until(ExpectedConditions.urlContains("/products"));

            boolean isCorrectPage = driver.getTitle().contains("Products") || driver.getCurrentUrl().contains("products");
            Assert.assertTrue(isCorrectPage, "Chưa vào đúng trang Products!");

        } catch (Exception e) {
            takeScreenshot("Menu_Products_Error");
            Assert.fail("Lỗi Menu Products: " + e.getMessage());
        }
    }

    // --- TEST 3: MENU CONTACT ---
    @Test(priority = 3)
    void test_menu_contact() {
        driver.get(getBaseUrlWithSlash());
        try {
            WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
            contactLink.click();
            wait.until(ExpectedConditions.urlContains("contact"));

            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Contact us')]")));
            Assert.assertTrue(heading.isDisplayed());

        } catch (Exception e) {
            takeScreenshot("Menu_Contact_Error");
            Assert.fail("Lỗi Menu Contact: " + e.getMessage());
        }
    }

    // --- TEST 4: ICON GIỎ HÀNG (Cần Login) ---
    @Test(priority = 4)
    void test_header_cart_icon() {
        ensureLoggedIn();
        driver.get(getBaseUrlWithSlash());

        try {
            WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//i[contains(@class, 'fa-shopping-basket')]/parent::a | //a[contains(@href, 'cart')]")
            ));
            cartIcon.click();
            wait.until(ExpectedConditions.urlContains("cart"));

        } catch (Exception e) {
            takeScreenshot("Header_CartIcon_Error");
            Assert.fail("Lỗi Cart Icon: " + e.getMessage());
        }
    }

    // --- TEST 5: NAVIGATION LOGIN ---
    @Test(priority = 5)
    void test_login_navigation() {
        driver.get(getBaseUrlWithSlash() + "logout"); // Logout trước
        driver.get(getBaseUrlWithSlash());

        try {
            List<WebElement> loginLinks = driver.findElements(By.partialLinkText("Login"));
            if (loginLinks.size() > 0) {
                loginLinks.get(0).click();
            } else {
                WebElement userIcon = driver.findElement(By.xpath("//i[contains(@class, 'fa-user')]/parent::a"));
                userIcon.click();
            }
            wait.until(ExpectedConditions.urlContains("login"));

            WebElement signInTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(), 'sign in')]")));
            Assert.assertTrue(signInTab.isDisplayed());

        } catch (Exception e) {
            takeScreenshot("Nav_To_Login_Error");
            Assert.fail("Lỗi điều hướng Login: " + e.getMessage());
        }
    }
}