package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
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

import static com.java.automation.utils.ScreenshotUtil.takeScreenshot;

@Listeners(TestListener.class)
public class CheckoutTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private LoginOrRegisterPage loginPage;
    private static final int TIMEOUT = 30;

    @BeforeMethod
    public void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
        // Khởi tạo Page Object để dùng chung logic Login chuẩn
        loginPage = new LoginOrRegisterPage(driver);
    }

    // --- HÀM HỖ TRỢ ---

    // Sử dụng hàm Login chuẩn từ Page Object thay vì tự viết lại
    private void ensureLoggedIn() {
        loginPage.navigateToLoginPage();

        // Nếu chưa ở trang login (tức là đã login rồi) thì thôi
        if (!loginPage.isOnLoginPage()) {
            return;
        }

        System.out.println("🔄 Đang thực hiện đăng nhập...");

        // Lấy user từ config hoặc dùng mặc định
        String user = TestConfig.getProperty("test.username");
        String pass = TestConfig.getProperty("test.password");
        if (user == null) user = "abcd";
        if (pass == null) pass = "123123";

        loginPage.login(user, pass);

        // Verify login thành công
        if (!loginPage.isOnHomePage()) {
            Assert.fail("Login thất bại: Không chuyển hướng về trang chủ sau khi đăng nhập.");
        }
    }

    private void ensureCartHasProduct() {
        driver.get(TestConfig.getBaseUrl() + "/carts");
        try {
            // Chờ bảng load hoặc thông báo empty
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            } catch (Exception ignored) {}

            List<WebElement> rows = driver.findElements(By.cssSelector("table.table-list tbody tr"));

            if (rows.isEmpty()) {
                System.out.println("⚠️ Giỏ hàng rỗng! Đang đi thêm sản phẩm...");
                driver.get(TestConfig.getBaseUrl() + "/products");

                // Thêm sản phẩm đầu tiên tìm thấy
                WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
                clickElementJS(addBtn);

                // Chờ xíu để server xử lý
                Thread.sleep(1500);

                // Quay lại check
                driver.get(TestConfig.getBaseUrl() + "/carts");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Warning check giỏ hàng: " + e.getMessage());
        }
    }

    // --- TEST CASES ---

    @Test(priority = 1)
    void test_checkout_process_success() {
        ensureLoggedIn();
        ensureCartHasProduct();

        System.out.println("👉 Bắt đầu Checkout...");
        driver.get(TestConfig.getBaseUrl() + "/checkout");

        try {
            // 1. Điền thông tin (Giữ nguyên code cũ của bạn)
            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("receiver")));
            nameInput.clear();
            nameInput.sendKeys("Test User Selenium");
            driver.findElement(By.name("address")).sendKeys("123 Testing Street");
            driver.findElement(By.name("phone")).sendKeys("0987654321");
            driver.findElement(By.name("description")).sendKeys("Giao hàng giờ hành chính");

            // 2. Click Place Order
            // Selector này đúng với HTML: <button ...><span>Place order</span></button>
            WebElement placeOrderBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'Place order') or contains(., 'Place Order')]")
            ));

            // Dùng JS click để chắc chắn submit form
            clickElementJS(placeOrderBtn);

            // 3. Verify Thành công
            // Quan trọng: Chờ URL thay đổi HOẶC trang Success hiện ra
            // File checkout_success.html có: <h4>Thank you for your purchase!</h4>
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("success"),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//h4[contains(text(), 'Thank you')]"))
            ));

            boolean isSuccess = driver.getCurrentUrl().contains("success") ||
                    driver.getPageSource().contains("Thank you") ||
                    driver.getPageSource().contains("Cảm ơn");

            Assert.assertTrue(isSuccess, "Checkout thất bại: Không thấy thông báo thành công!");

        } catch (Exception e) {
            takeScreenshot("Checkout_Success_Fail");
            Assert.fail("Lỗi quá trình Checkout: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    void test_checkout_fail_missing_address() {
        ensureLoggedIn();
        ensureCartHasProduct();

        driver.get(TestConfig.getBaseUrl() + "/checkout");

        try {
            System.out.println("👉 Test Checkout thiếu địa chỉ...");

            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("receiver")));
            nameInput.clear();
            nameInput.sendKeys("User Missing Address");
            driver.findElement(By.name("phone")).sendKeys("0123456789");

            // Xóa địa chỉ để gây lỗi
            WebElement addressInput = driver.findElement(By.name("address"));
            addressInput.clear();

            WebElement placeOrderBtn = driver.findElement(By.xpath("//button[contains(., 'Place order') or contains(., 'Đặt hàng')]"));
            clickElementJS(placeOrderBtn);

            // Chờ 1 chút xem có chuyển trang không
            Thread.sleep(2000);

            // Verify: Vẫn phải ở lại trang checkout
            String currentUrl = driver.getCurrentUrl();
            boolean stayedOnPage = currentUrl.contains("checkout") && !currentUrl.contains("success");

            // Có thể check thêm HTML5 validation message nếu cần
            // String validationMsg = addressInput.getAttribute("validationMessage");

            if (stayedOnPage) {
                System.out.println("✅ Pass: Hệ thống đã chặn checkout khi thiếu địa chỉ.");
            } else {
                takeScreenshot("Checkout_MissingInfo_Fail");
                Assert.fail("Lỗi: Hệ thống cho phép checkout khi thiếu địa chỉ! URL hiện tại: " + currentUrl);
            }

        } catch (Exception e) {
            Assert.fail("Lỗi test case thiếu thông tin: " + e.getMessage());
        }
    }
}