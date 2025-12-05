package com.java.automation.selenium;

import com.java.automation.selenium.BaseSeleniumTest;
import com.java.automation.selenium.TestListener;
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
    private static final int TIMEOUT = 10;

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

    // --- LOGIC LOGIN ---
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
            System.out.println("Đã login hoặc có lỗi login: " + e.getMessage());
        }
    }

    // --- TEST CASE 1: THÊM SẢN PHẨM ---
    @Test(priority = 1)
    void test_add_to_cart_success() {
        ensureLoggedIn();
        driver.get(BASE_URL + "products");

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
            List<WebElement> addButtons = driver.findElements(By.cssSelector(".product-btn a"));

            if (addButtons.isEmpty()) Assert.fail("Không tìm thấy sản phẩm nào!");

            // Lấy sản phẩm thứ 2 (index 1)
            WebElement btnAddToCart = addButtons.size() > 1 ? addButtons.get(1) : addButtons.get(0);

            System.out.println("Đang click Add to Cart...");
            clickElementJS(btnAddToCart);

            // Chờ 1.5s để server xử lý request thêm hàng
            Thread.sleep(1500);

            // Chủ động chuyển hướng sang trang Cart
            driver.get(BASE_URL + "carts");

            // Validate
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.table-list")));

            int rowCount = driver.findElements(By.cssSelector("table.table-list tbody tr")).size();
            Assert.assertTrue(rowCount > 0, "Giỏ hàng vẫn trống sau khi thêm!");

        } catch (Exception e) {
            Assert.fail("Lỗi Add Cart: " + e.getMessage());
        }
    }

    // --- TEST CASE 2: CẬP NHẬT SỐ LƯỢNG ---
    @Test(priority = 2)
    void test_update_quantity() {
        if (!driver.getCurrentUrl().contains("cart")) {
            driver.get(BASE_URL + "carts");
        }

        try {
            WebElement qtyInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@id, 'quantityInput_')]")
            ));

            qtyInput.clear();
            qtyInput.sendKeys("5");

            // Bấm Enter để trigger onchange
            qtyInput.sendKeys(Keys.ENTER);
            Thread.sleep(1000);

            String val = qtyInput.getAttribute("value");
            Assert.assertEquals(val, "5", "Số lượng chưa được cập nhật!");

        } catch (Exception e) {
            Assert.fail("Lỗi Update Cart: " + e.getMessage());
        }
    }

    // --- TEST CASE 3: XÓA SẢN PHẨM ---
    @Test(priority = 3)
    void test_remove_from_cart() {
        if (!driver.getCurrentUrl().contains("cart")) {
            driver.get(BASE_URL + "carts");
        }

        try {
            int oldSize = driver.findElements(By.cssSelector("table.table-list tbody tr")).size();
            if (oldSize == 0) Assert.fail("Giỏ hàng rỗng!");

            WebElement trashBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".fa-trash-alt")
            ));

            clickElementJS(trashBtn);

            // Xử lý Modal Confirm
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));

            WebElement yesBtn = driver.findElement(By.id("yesOption"));
            wait.until(ExpectedConditions.elementToBeClickable(yesBtn));
            yesBtn.click();

            // Chờ reload
            Thread.sleep(1500);
            int newSize = driver.findElements(By.cssSelector("table.table-list tbody tr")).size();

            Assert.assertTrue(newSize < oldSize, "Sản phẩm vẫn còn, chưa bị xóa!");

        } catch (Exception e) {
            Assert.fail("Lỗi Xóa Cart: " + e.getMessage());
        }
    }
}