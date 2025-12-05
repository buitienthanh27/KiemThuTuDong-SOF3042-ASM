package com.java.automation.selenium;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ExtendWith(ScreenshotOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CheckoutTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 15;

    @BeforeEach
    void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    public void takeScreenshot(String fileName, String pass) {
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

    // --- HÀM CLICK JS (Trị các nút bị che) ---
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
        driver.get("http://localhost:9090/login");
        try {
            if (!driver.getCurrentUrl().contains("login")) return;

            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            userField.sendKeys("abcd"); // User của bạn

            driver.findElement(By.name("password")).sendKeys("123123");

            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            clickElementJS(loginBtn);

            wait.until(ExpectedConditions.urlToBe("http://localhost:9090/"));
        } catch (Exception e) {
            System.out.println("Login info: " + e.getMessage());
        }
    }

    // --- 2. ĐẢM BẢO GIỎ HÀNG CÓ SẢN PHẨM ---
    private void ensureCartHasProduct() {
        driver.get("http://localhost:9090/carts");
        try {
            // Kiểm tra bảng giỏ hàng
            List<WebElement> rows = driver.findElements(By.cssSelector("table.table-list tbody tr"));

            if (rows.isEmpty()) {
                System.out.println("⚠️ Giỏ hàng rỗng! Đang tự động thêm sản phẩm...");
                driver.get("http://localhost:9090/products");

                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
                List<WebElement> addButtons = driver.findElements(By.cssSelector(".product-btn a"));

                if (!addButtons.isEmpty()) {
                    clickElementJS(addButtons.get(0));
                    Thread.sleep(1500); // Chờ server xử lý
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi kiểm tra giỏ hàng: " + e.getMessage());
        }
    }

    // --- TEST CASE: CHECKOUT ---
    @Test
    @Order(1)
    void test_checkout_process_success() {
        ensureLoggedIn();
        ensureCartHasProduct(); // Pre-condition: Phải có hàng mới checkout được

        // Vào trang checkout
        driver.get("http://localhost:9090/checkout");

        try {
            // 1. Điền Form Shipping (Dựa trên checkOut.html)
            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("receiver")));
            nameInput.clear();
            nameInput.sendKeys("Test User Selenium");

            driver.findElement(By.name("address")).sendKeys("123 Testing Street");
            driver.findElement(By.name("phone")).sendKeys("0987654321");
            driver.findElement(By.name("description")).sendKeys("Giao hàng giờ hành chính");

            // 2. Submit Order
            WebElement placeOrderBtn = driver.findElement(By.xpath("//button[contains(., 'Place order')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", placeOrderBtn);
            Thread.sleep(1000);

            clickElementJS(placeOrderBtn);

            // 3. Validate Success Page (Dựa trên checkout_success.html)
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("success"),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[contains(text(), 'Thank you')]"))
            ));

            boolean isSuccess = driver.getPageSource().contains("Thank you for your purchase");
            Assertions.assertTrue(isSuccess, "Thất bại: Không thấy thông báo 'Thank you'!");

            // Lấy Order ID in ra console chơi
            try {
                String orderId = driver.findElement(By.xpath("//h5/span")).getText();
                System.out.println("🎉 ORDER SUCCESS! ID: " + orderId);
            } catch (Exception ignored) {}

            takeScreenshot("Checkout_Success", "PASS");

        } catch (Exception e) {
            takeScreenshot("Checkout_Fail", "FAIL");
            Assertions.fail("Lỗi Checkout: " + e.getMessage());
        }
    }

    // --- TEST CASE 2: THANH TOÁN THẤT BẠI (THIẾU ĐỊA CHỈ) - SỬA LỖI CHỤP ẢNH ---
    @Test
    @Order(2)
    void test_checkout_fail_missing_address() {
        ensureLoggedIn();
        ensureCartHasProduct();

        driver.get("http://localhost:9090/checkout");

        try {
            System.out.println("Test 2: Thử thanh toán thiếu Address...");

            // 1. Điền thông tin (trừ Address)
            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("receiver")));
            nameInput.clear();
            nameInput.sendKeys("User Test Fail");

            driver.findElement(By.name("phone")).sendKeys("0123456789");

            // CỐ TÌNH ĐỂ TRỐNG ADDRESS

            // 2. Click Place Order (Nằm ở dưới cùng)
            WebElement placeOrderBtn = driver.findElement(By.xpath("//button[contains(., 'Place order')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", placeOrderBtn);
            Thread.sleep(500);
            placeOrderBtn.click(); // Click kích hoạt validate

            Thread.sleep(1000);

            // 3. --- QUAN TRỌNG: SCROLL LÊN ĐỂ CHỤP ẢNH ĐÚNG CHỖ ---
            // Tìm lại ô Address hoặc tiêu đề form để cuộn lên đó
            WebElement addressInput = driver.findElement(By.name("address"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addressInput);

            // Chờ xíu cho màn hình cuộn xong
            Thread.sleep(500);

            // 4. Validate và Chụp ảnh
            String currentUrl = driver.getCurrentUrl();
            boolean stayedOnPage = currentUrl.contains("checkout") || !currentUrl.contains("success");

            Assertions.assertTrue(stayedOnPage, "Lỗi: Hệ thống không chặn khi thiếu Address!");

            // Giờ chụp ảnh sẽ thấy ngay ô Address đang bị trống (và có thể thấy bong bóng lỗi)
            takeScreenshot("Checkout_MissingAddress_Blocked", "PASS");

        } catch (Exception e) {
            // Nếu lỗi văng ra Exception (ví dụ không tìm thấy element), ta cũng nên scroll lên đầu trang để xem
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}

            takeScreenshot("Checkout_MissingAddress_Error", "FAIL");
            Assertions.fail("Lỗi Test: " + e.getMessage());
        }
    }
}