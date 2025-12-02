package com.java.selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ExtendWith(ScreenshotOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NavigationTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 10;

    @BeforeEach
    void setUp() {
        wait = new WebDriverWait(driver, TIMEOUT);
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

    public void takeScreenshot(String fileName) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            // Mặc định là FAIL vì giờ ta chỉ chụp khi lỗi
            String fullFileName = "screenshots/ERROR_" + fileName + "_" + timestamp + ".png";
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = Paths.get(fullFileName);
            Files.createDirectories(destination.getParent());
            Files.copy(scrFile.toPath(), destination);
            System.err.println("📸 Đã chụp ảnh lỗi: " + fullFileName);
        } catch (Exception e) {
            System.err.println("Lỗi chụp ảnh: " + e.getMessage());
        }
    }

    private void ensureLoggedIn() {
        driver.get("http://localhost:8080/login");
        try {
            if (!driver.getCurrentUrl().contains("login")) return;
            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            userField.sendKeys("abcd");
            driver.findElement(By.name("password")).sendKeys("123123");
            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            clickElementJS(loginBtn);
            wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        } catch (Exception e) {
            System.out.println("Login info: " + e.getMessage());
        }
    }

    // --- TEST 1: LOGO ---
    @Test
    @Order(1)
    void test_logo_redirects_to_home() {
        driver.get("http://localhost:8080/contact");
        try {
            WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='logo']/parent::a")));
            logo.click();
            wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
            Assertions.assertEquals("http://localhost:8080/", driver.getCurrentUrl());

            // Đã xóa chụp ảnh PASS

        } catch (Exception e) {
            takeScreenshot("Logo_Error"); // Chỉ chụp khi lỗi
            Assertions.fail("Lỗi Logo: " + e.getMessage());
        }
    }

    // --- TEST 2: MENU ALL PRODUCTS ---
    @Test
    @Order(2)
    void test_menu_all_products() {
        driver.get("http://localhost:8080/");
        try {
            WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Products")));
            productLink.click();
            wait.until(ExpectedConditions.urlContains("/products"));
            boolean isCorrectPage = driver.getTitle().contains("Products") || driver.getCurrentUrl().contains("products");
            Assertions.assertTrue(isCorrectPage, "Chưa vào đúng trang Products!");

        } catch (Exception e) {
            takeScreenshot("Menu_Products_Error");
            Assertions.fail("Lỗi Menu Products: " + e.getMessage());
        }
    }

    // --- TEST 3: MENU CONTACT ---
    @Test
    @Order(3)
    void test_menu_contact() {
        driver.get("http://localhost:8080/");
        try {
            WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
            contactLink.click();
            wait.until(ExpectedConditions.urlContains("contact"));
            WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Contact us')]")));
            Assertions.assertTrue(heading.isDisplayed());

        } catch (Exception e) {
            takeScreenshot("Menu_Contact_Error");
            Assertions.fail("Lỗi Menu Contact: " + e.getMessage());
        }
    }

    // --- TEST 4: ICON GIỎ HÀNG (Cần Login) ---
    @Test
    @Order(4)
    void test_header_cart_icon() {
        ensureLoggedIn(); // Gọi hàm này để tránh lỗi timeout
        driver.get("http://localhost:8080/");

        try {
            WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//i[contains(@class, 'fa-shopping-basket')]/parent::a | //a[contains(@href, 'cart')]")
            ));
            cartIcon.click();
            wait.until(ExpectedConditions.urlContains("cart"));

        } catch (Exception e) {
            takeScreenshot("Header_CartIcon_Error");
            Assertions.fail("Lỗi Cart Icon: " + e.getMessage());
        }
    }

    // --- TEST 5: NAVIGATION LOGIN ---
    @Test
    @Order(5)
    void test_login_navigation() {
        driver.get("http://localhost:8080/logout"); // Logout trước
        driver.get("http://localhost:8080/");

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
            Assertions.assertTrue(signInTab.isDisplayed());

        } catch (Exception e) {
            takeScreenshot("Nav_To_Login_Error");
            Assertions.fail("Lỗi điều hướng Login: " + e.getMessage());
        }
    }
}