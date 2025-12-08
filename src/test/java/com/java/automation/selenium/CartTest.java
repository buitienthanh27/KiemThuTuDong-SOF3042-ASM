package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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
public class CartTest extends BaseSeleniumTest {

    private WebDriverWait wait;

    @BeforeMethod
    public void setUpTest() {
        // Tăng timeout lên 30s để đảm bảo tìm thấy element trên CI chậm
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // --- HELPER METHODS ---

    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    private void loginAsCustomer() {
        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();

        // Lấy user thường từ config
        String userId = TestConfig.getProperty("test.user.id");
        String password = TestConfig.getProperty("test.user.password");

        System.out.println("🔄 Login User: " + userId);
        loginPage.login(userId, password);

        // Chờ về trang chủ
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe(BASE_URL),
                    ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("Logout"))
            ));

            if (driver.getCurrentUrl().contains("login")) {
                driver.get(BASE_URL);
            }
        } catch (Exception e) {
            Assert.fail("Login User thất bại!");
        }
    }

    // Hàm kiểm tra và thêm sản phẩm nếu giỏ hàng rỗng
    private void addProductToCartIfNeeded() {
        driver.get(BASE_URL + "carts"); // SỬA: carts (số nhiều)

        // Kiểm tra nếu giỏ hàng trống (dựa trên bảng hoặc text thông báo)
        if (driver.getPageSource().contains("Giỏ hàng của bạn đang trống") ||
                driver.findElements(By.cssSelector("table tbody tr")).isEmpty()) {

            System.out.println("⚠️ Giỏ hàng rỗng! Đang tự động thêm sản phẩm...");
            driver.get(BASE_URL + "products");

            try {
                // Tìm nút Add to Cart (thử locator theo class cũ của bạn)
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
                List<WebElement> addButtons = driver.findElements(By.cssSelector(".product-btn a"));

                if (!addButtons.isEmpty()) {
                    WebElement btn = addButtons.get(0);
                    clickElementJS(btn);
                    Thread.sleep(1500); // Chờ server xử lý
                }
            } catch (Exception e) {
                System.out.println("⚠️ Không tìm thấy nút thêm giỏ hàng!");
            }
        }
    }

    // --- TEST CASES ---

    @Test(priority = 1)
    public void test_add_to_cart_success() {
        loginAsCustomer();

        System.out.println("Đang click Add to Cart...");
        driver.get(BASE_URL + "products");

        try {
            // Locator cũ của bạn: .product-btn a
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
            List<WebElement> addButtons = driver.findElements(By.cssSelector(".product-btn a"));

            if (addButtons.isEmpty()) Assert.fail("Không tìm thấy sản phẩm nào để thêm!");

            // Lấy sản phẩm đầu tiên
            WebElement btnAddToCart = addButtons.get(0);
            clickElementJS(btnAddToCart);

            // Chờ server xử lý và redirect
            Thread.sleep(1500);

            // Vào trang giỏ hàng để kiểm tra
            driver.get(BASE_URL + "carts"); // SỬA: carts

            // Validate có bảng sản phẩm
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
            int rowCount = driver.findElements(By.cssSelector("table tbody tr")).size();

            Assert.assertTrue(rowCount > 0, "Giỏ hàng vẫn trống sau khi thêm!");
            System.out.println("✅ Thêm vào giỏ thành công.");

        } catch (Exception e) {
            takeScreenshot("Add_To_Cart_Fail");
            Assert.fail("Lỗi Add Cart: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    public void test_update_quantity() {
        loginAsCustomer();
        addProductToCartIfNeeded();

        driver.get(BASE_URL + "carts"); // SỬA: carts

        try {
            // Tìm ô input số lượng
            WebElement qtyInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@id, 'quantityInput')]")
            ));

            qtyInput.clear();
            qtyInput.sendKeys("5");

            // SỬA: Dùng Keys.ENTER như code cũ của bạn
            qtyInput.sendKeys(Keys.ENTER);

            Thread.sleep(1500); // Chờ reload

            // Kiểm tra lại giá trị
            driver.navigate().refresh();
            WebElement qtyAfter = driver.findElement(By.xpath("//input[contains(@id, 'quantityInput')]"));

            Assert.assertEquals(qtyAfter.getAttribute("value"), "5", "Số lượng không cập nhật!");
            System.out.println("✅ Cập nhật số lượng thành công.");

        } catch (Exception e) {
            takeScreenshot("Update_Cart_Fail");
            Assert.fail("Lỗi Update Cart: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    public void test_remove_from_cart() {
        loginAsCustomer();
        addProductToCartIfNeeded();

        driver.get(BASE_URL + "carts"); // SỬA: carts

        try {
            int oldSize = driver.findElements(By.cssSelector("table tbody tr")).size();

            // Tìm nút xóa (Icon thùng rác .fa-trash-alt như code cũ)
            WebElement trashBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".fa-trash-alt") // Hoặc thẻ a chứa href remove
            ));

            // Click nút xóa
            // Lưu ý: Nút xóa thường nằm trong thẻ <a> hoặc <button>, click vào phần tử cha nếu cần
            WebElement parentLink = trashBtn.findElement(By.xpath("./.."));
            clickElementJS(parentLink);

            // SỬA: Xử lý Modal Confirm (configmationId) thay vì Alert
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));
            WebElement yesBtn = driver.findElement(By.id("yesOption"));
            wait.until(ExpectedConditions.elementToBeClickable(yesBtn));
            yesBtn.click();

            Thread.sleep(1500); // Chờ xóa xong

            int newSize = driver.findElements(By.cssSelector("table tbody tr")).size();

            Assert.assertTrue(newSize < oldSize, "Sản phẩm vẫn còn, chưa bị xóa!");
            System.out.println("✅ Xóa sản phẩm thành công.");

        } catch (Exception e) {
            takeScreenshot("Remove_Cart_Fail");
            Assert.fail("Lỗi Remove Cart: " + e.getMessage());
        }
    }
}