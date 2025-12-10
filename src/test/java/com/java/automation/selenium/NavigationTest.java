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

// Import để dùng hàm chụp ảnh nếu cần
import static com.java.automation.utils.ScreenshotUtil.takeScreenshot;

@Listeners(TestListener.class)
public class NavigationTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 20; // Tăng timeout lên chút cho an toàn

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
            // Scroll đến phần tử để chắc chắn nó nằm trong viewport
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500); // Chờ scroll và animation
            // Click bằng JS
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            // Fallback nếu JS fail (hiếm khi xảy ra)
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
            if (!driver.getCurrentUrl().contains("login")) return;

            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            // Lấy user từ config hoặc fallback
            String user = TestConfig.getProperty("test.username");
            String pass = TestConfig.getProperty("test.password");
            userField.sendKeys(user != null ? user : "abcd");
            driver.findElement(By.name("password")).sendKeys(pass != null ? pass : "123123");

            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            clickElementJS(loginBtn);

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
        waitForPageLoaded();
        try {
            WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='logo']/parent::a")));

            // Dùng click JS cho chắc chắn
            clickElementJS(logo);

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
        waitForPageLoaded();
        try {
            WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Products")));
            clickElementJS(productLink);

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
        waitForPageLoaded();
        try {
            WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
            clickElementJS(contactLink);

            wait.until(ExpectedConditions.urlContains("contact"));

            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Contact us')]")));
            Assert.assertTrue(heading.isDisplayed());

        } catch (Exception e) {
            takeScreenshot("Menu_Contact_Error");
            Assert.fail("Lỗi Menu Contact: " + e.getMessage());
        }
    }

    // --- TEST 4: ICON GIỎ HÀNG (ĐÃ SỬA LỖI) ---
    @Test(priority = 4)
    void test_header_cart_icon() {
        ensureLoggedIn();
        driver.get(getBaseUrlWithSlash());
        waitForPageLoaded(); // Chờ trang load xong hoàn toàn

        try {
            // SỬA: Tìm thẻ 'a' chứa href có chữ 'cart'. Selector này bao quát hơn.
            // Đồng thời dùng visibilityOfElementLocated để đảm bảo nó hiển thị.
            WebElement cartIcon = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//a[contains(@href, 'cart') or contains(@href, 'carts')]")
            ));

            // SỬA QUAN TRỌNG: Dùng click JS để xuyên qua mọi lớp phủ (badge, span, animation...)
            clickElementJS(cartIcon);

            // Verify
            wait.until(ExpectedConditions.urlContains("cart"));

        } catch (Exception e) {
            takeScreenshot("Header_CartIcon_Error");
            Assert.fail("Lỗi Cart Icon: " + e.getMessage());
        }
    }

    // --- TEST 5: NAVIGATION LOGIN ---
    @Test(priority = 5)
    void test_login_navigation() {
        driver.get(getBaseUrlWithSlash() + "logout");
        driver.get(getBaseUrlWithSlash());
        waitForPageLoaded();

        try {
            List<WebElement> loginLinks = driver.findElements(By.partialLinkText("Login"));
            if (loginLinks.size() > 0) {
                clickElementJS(loginLinks.get(0));
            } else {
                // Fallback tìm icon user
                WebElement userIcon = driver.findElement(By.xpath("//i[contains(@class, 'fa-user')]/parent::a"));
                clickElementJS(userIcon);
            }
            wait.until(ExpectedConditions.urlContains("login"));

            // Tìm tab "Sign In" hoặc "Đăng nhập"
            WebElement signInTab = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//a[contains(text(), 'sign in') or contains(text(), 'Sign In')]")
            ));
            Assert.assertTrue(signInTab.isDisplayed());

        } catch (Exception e) {
            takeScreenshot("Nav_To_Login_Error");
            Assert.fail("Lỗi điều hướng Login: " + e.getMessage());
        }
    }
}