package com.java.automation.selenium;

import com.java.automation.selenium.BaseSeleniumTest;
import com.java.automation.selenium.TestListener;
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
public class CheckoutTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 15;

    @BeforeMethod
    void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    // --- HÀM CLICK JS ---
    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    // --- 1. LOGIN ---
    private void ensureLoggedIn() {
        driver.get(BASE_URL + "login");
        try {
            if (!driver.getCurrentUrl().contains("login")) return;

            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            userField.sendKeys("abcd");

            driver.findElement(By.name("password")).sendKeys("123123");

            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            clickElementJS(loginBtn);

            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        } catch (Exception e) {
            System.out.println("Login info: " + e.getMessage());
        }
    }

    // --- 2. ĐẢM BẢO GIỎ HÀNG CÓ SẢN PHẨM ---
    private void ensureCartHasProduct() {
        driver.get(BASE_URL + "carts");
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.table-list tbody tr"));

            if (rows.isEmpty()) {
                System.out.println("⚠️ Giỏ hàng rỗng! Đang tự động thêm sản phẩm...");
                driver.get(BASE_URL + "products");

                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
                List<WebElement> addButtons = driver.findElements(By.cssSelector(".product-btn a"));

                if (!addButtons.isEmpty()) {
                    clickElementJS(addButtons.get(0));
                    Thread.sleep(1500);
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi kiểm tra giỏ hàng: " + e.getMessage());
        }
    }

    // --- TEST CASE 1: CHECKOUT SUCCESS ---
    @Test(priority = 1)
    void test_checkout_process_success() {
        ensureLoggedIn();
        ensureCartHasProduct();

        driver.get(BASE_URL + "checkout");

        try {
            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("receiver")));
            nameInput.clear();
            nameInput.sendKeys("Test User Selenium");

            driver.findElement(By.name("address")).sendKeys("123 Testing Street");
            driver.findElement(By.name("phone")).sendKeys("0987654321");
            driver.findElement(By.name("description")).sendKeys("Giao hàng giờ hành chính");

            WebElement placeOrderBtn = driver.findElement(By.xpath("//button[contains(., 'Place order')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", placeOrderBtn);
            Thread.sleep(1000);

            clickElementJS(placeOrderBtn);

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("success"),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[contains(text(), 'Thank you')]"))
            ));

            boolean isSuccess = driver.getPageSource().contains("Thank you for your purchase");
            Assert.assertTrue(isSuccess, "Thất bại: Không thấy thông báo 'Thank you'!");

            try {
                String orderId = driver.findElement(By.xpath("//h5/span")).getText();
                System.out.println("🎉 ORDER SUCCESS! ID: " + orderId);
            } catch (Exception ignored) {}

            // Dùng hàm takeScreenshot của lớp cha (BaseSeleniumTest)
            takeScreenshot("Checkout_Success");

        } catch (Exception e) {
            takeScreenshot("Checkout_Fail");
            Assert.fail("Lỗi Checkout: " + e.getMessage());
        }
    }

    // --- TEST CASE 2: CHECKOUT FAIL (MISSING ADDRESS) ---
    @Test(priority = 2)
    void test_checkout_fail_missing_address() {
        ensureLoggedIn();
        ensureCartHasProduct();

        driver.get(BASE_URL + "checkout");

        try {
            System.out.println("Test 2: Thử thanh toán thiếu Address...");

            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("receiver")));
            nameInput.clear();
            nameInput.sendKeys("User Test Fail");
            driver.findElement(By.name("phone")).sendKeys("0123456789");

            // CỐ TÌNH ĐỂ TRỐNG ADDRESS

            WebElement placeOrderBtn = driver.findElement(By.xpath("//button[contains(., 'Place order')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", placeOrderBtn);
            Thread.sleep(500);
            placeOrderBtn.click(); // Click thường để trigger validation

            Thread.sleep(1000);

            // Scroll lên để chụp ảnh lỗi
            WebElement addressInput = driver.findElement(By.name("address"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addressInput);
            Thread.sleep(500);

            String currentUrl = driver.getCurrentUrl();
            boolean stayedOnPage = currentUrl.contains("checkout") || !currentUrl.contains("success");

            if (stayedOnPage) {
                System.out.println("Pass: Hệ thống chặn thành công.");
                takeScreenshot("Checkout_MissingAddress_Blocked");
            } else {
                takeScreenshot("Checkout_MissingAddress_FAIL");
                Assert.fail("Lỗi: Hệ thống không chặn khi thiếu Address!");
            }

        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}

            takeScreenshot("Checkout_MissingAddress_Error");
            Assert.fail("Lỗi Test: " + e.getMessage());
        }
    }
}