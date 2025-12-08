package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.openqa.selenium.By;
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
    private LoginOrRegisterPage loginPage;

    @BeforeMethod
    public void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
        driver.get(TestConfig.getBaseUrl() + "/products");
        waitForPageLoaded(); // Chờ JS load xong

        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
        smartClick(addBtn); // Dùng hàm click thông minh

        // Chờ server xử lý
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        driver.get(TestConfig.getBaseUrl() + "/carts");
        waitForPageLoaded();

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            Assert.assertFalse(rows.isEmpty(), "Giỏ hàng vẫn trống sau khi thêm!");
        } catch (Exception e) {
            takeScreenshot("Add_To_Cart_Fail");
            Assert.fail("Lỗi kiểm tra giỏ hàng: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    public void test_update_quantity() {
        ensureLoggedIn();
        driver.get(TestConfig.getBaseUrl() + "/carts");
        waitForPageLoaded();

        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        if (rows.isEmpty()) {
            test_add_to_cart_success();
            driver.get(TestConfig.getBaseUrl() + "/carts");
        }

        try {
            WebElement qtyInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='number']")));
            qtyInput.clear();
            qtyInput.sendKeys("5");
            // FIX: Nhấn Enter để trigger update form
            qtyInput.sendKeys(Keys.ENTER);

            // Chờ server reload lại trang hoặc xử lý ajax
            Thread.sleep(2000);

            // Refresh để chắc chắn lấy data mới từ server
            driver.navigate().refresh();
            waitForPageLoaded();

            WebElement qtyInputAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='number']")));
            Assert.assertEquals(qtyInputAfter.getAttribute("value"), "5", "Số lượng không cập nhật thành 5!");

        } catch (Exception e) {
            takeScreenshot("Update_Cart_Fail");
            Assert.fail("Lỗi update giỏ hàng: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    public void test_remove_from_cart() {
        ensureLoggedIn();
        driver.get(TestConfig.getBaseUrl() + "/carts");
        waitForPageLoaded();

        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        if (rows.isEmpty()) {
            test_add_to_cart_success();
            driver.get(TestConfig.getBaseUrl() + "/carts");
            rows = driver.findElements(By.cssSelector("table tbody tr"));
        }

        int beforeDelete = rows.size();

        try {
            WebElement removeBtn = driver.findElement(By.xpath("//a[contains(@href, 'remove') or .//i[contains(@class, 'trash')]]"));
            smartClick(removeBtn);

            Thread.sleep(2000);
            driver.navigate().refresh();
            waitForPageLoaded();

            List<WebElement> rowsAfter = driver.findElements(By.cssSelector("table tbody tr"));
            Assert.assertTrue(rowsAfter.size() < beforeDelete, "Số lượng sản phẩm không giảm sau khi xóa!");

        } catch (Exception e) {
            takeScreenshot("Remove_Cart_Fail");
            Assert.fail("Lỗi xóa sản phẩm: " + e.getMessage());
        }
    }
}