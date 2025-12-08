package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.openqa.selenium.By;
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
    private LoginOrRegisterPage loginPage;

    @BeforeMethod
    public void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        loginPage = new LoginOrRegisterPage(driver);
    }

    private void ensureLoggedIn() {
        loginPage.navigateToLoginPage();
        if (!loginPage.isOnLoginPage()) return;

        String user = TestConfig.getProperty("test.username");
        String pass = TestConfig.getProperty("test.password");
        loginPage.login(user == null ? "abcd" : user, pass == null ? "123123" : pass);
    }

    @Test(priority = 1)
    public void test_add_to_cart_success() {
        ensureLoggedIn();

        // 1. Vào trang sản phẩm
        driver.get(TestConfig.getBaseUrl() + "/products");

        // 2. Tìm và click nút thêm vào giỏ
        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
        clickElementJS(addBtn);

        // --- FIX QUAN TRỌNG: Chờ server xử lý ---
        // Cách 1: Chờ thông báo alert (nếu có)
        try {
            // Chờ 2 giây cứng để chắc chắn server đã lưu DB
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        // 3. Vào giỏ hàng kiểm tra
        driver.get(TestConfig.getBaseUrl() + "/carts");

        try {
            // Chờ bảng hiển thị
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));

            Assert.assertFalse(rows.isEmpty(), "Giỏ hàng vẫn trống sau khi thêm!");

            // Check thêm: Cột tên sản phẩm có dữ liệu
            WebElement productName = rows.get(0).findElement(By.cssSelector("td:nth-child(2)")); // Giả sử cột 2 là tên
            Assert.assertTrue(productName.getText().length() > 0, "Tên sản phẩm bị rỗng");

        } catch (Exception e) {
            takeScreenshot("Add_To_Cart_Fail");
            Assert.fail("Lỗi kiểm tra giỏ hàng: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    public void test_update_quantity() {
        ensureLoggedIn();
        driver.get(TestConfig.getBaseUrl() + "/carts");

        // Đảm bảo có hàng để update
        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        if (rows.isEmpty()) {
            test_add_to_cart_success(); // Gọi lại hàm thêm nếu rỗng
            driver.get(TestConfig.getBaseUrl() + "/carts");
        }

        try {
            // Tìm ô input số lượng
            WebElement qtyInput = driver.findElement(By.cssSelector("input[type='number']"));
            qtyInput.clear();
            qtyInput.sendKeys("5");

            // Tìm nút update (thường là icon hoặc nút bên cạnh)
            // Giả sử update tự động khi blur hoặc có nút Update
            // Nếu có nút Update Cart:
            try {
                WebElement updateBtn = driver.findElement(By.xpath("//button[contains(text(),'Update') or contains(@class,'fa-sync')]"));
                clickElementJS(updateBtn);
                Thread.sleep(1500);
            } catch (Exception ex) {
                // Nếu update bằng Ajax khi đổi số: click ra ngoài
                driver.findElement(By.tagName("h2")).click();
                Thread.sleep(1500);
            }

            // Reload và check lại giá trị
            driver.navigate().refresh();
            WebElement qtyInputAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='number']")));
            Assert.assertEquals(qtyInputAfter.getAttribute("value"), "5", "Số lượng không cập nhật thành 5!");

        } catch (Exception e) {
            takeScreenshot("Update_Cart_Fail");
            Assert.fail("Lỗi update giỏ hàng: " + e.getMessage());
        }
    }
}