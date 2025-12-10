package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

@Listeners(TestListener.class)
public class AdminDashboardTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private LoginOrRegisterPage loginPage;

    @BeforeMethod
    public void setUp() {
        // Tăng timeout lên 30s cho môi trường CI chậm
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        loginPage = new LoginOrRegisterPage(driver);
    }

    // --- HÀM HỖ TRỢ ---

    // Chờ DataTables load xong hoàn toàn
    private void waitForDataTableLoaded() {
        try {
            // Chờ thanh search hoặc phân trang xuất hiện -> Dấu hiệu bảng đã render xong
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_paginate"))
            ));
        } catch (Exception e) {
            System.out.println("⚠️ Warning: DataTable load chậm hoặc bảng rỗng.");
        }
    }

    private void openAddRowModal() {
        By modalLocator = By.id("addRowModal");
        // CSS Selector chính xác hơn cho nút thêm
        By btnLocator = By.cssSelector("button[data-target='#addRowModal']");

        waitForPageLoaded();

        for (int i = 0; i < 3; i++) {
            try {
                // 1. Chờ nút Clickable (quan trọng hơn Presence)
                WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(btnLocator));

                // 2. Click
                smartClick(addBtn);

                // 3. Chờ Modal hiển thị
                wait.until(ExpectedConditions.visibilityOfElementLocated(modalLocator));

                // 4. Kiểm tra chắc chắn modal đang hiển thị
                if(driver.findElement(modalLocator).isDisplayed()) {
                    return;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Mở modal thất bại lần " + (i + 1) + ", refresh và thử lại...");
                driver.navigate().refresh();
                waitForPageLoaded();
                // Chờ lại bảng load xong mới tìm nút
                waitForDataTableLoaded();
            }
        }
        Assert.fail("Lỗi: Không thể mở Modal thêm mới sau 3 lần thử!");
    }

    private void loginAsAdmin() {
        loginPage.navigateToLoginPage();
        // Kiểm tra nếu chưa login hoặc không phải trang admin
        if (!driver.getCurrentUrl().contains("admin")) {
            String user = TestConfig.getProperty("admin.username");
            String pass = TestConfig.getProperty("admin.password");
            loginPage.login(user == null ? "admin" : user, pass == null ? "123123" : pass);

            // Chờ redirect vào admin
            wait.until(ExpectedConditions.urlContains("admin"));
        }
    }

    // --- TEST CASES ---

    @Test(priority = 1)
    void test_access_admin_dashboard() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/home");
        waitForPageLoaded();
        try {
            WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Dashboard')]")));
            Assert.assertTrue(title.isDisplayed());
        } catch (Exception e) {
            takeScreenshot("Admin_Dashboard_Access_Fail");
            Assert.fail("Không vào được Dashboard: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    void test_product_crud() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");
        waitForPageLoaded();
        waitForDataTableLoaded(); // Đảm bảo bảng hiện ra trước khi thao tác

        try {
            System.out.println("Test 2.1: Thêm sản phẩm...");
            openAddRowModal();

            String proName = "Auto " + System.currentTimeMillis();

            // Wait cho các field trong modal sẵn sàng
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys(proName);
            driver.findElement(By.id("price")).sendKeys("150");
            driver.findElement(By.id("quantity")).sendKeys("10");
            driver.findElement(By.id("description")).sendKeys("Desc");

            try {
                new Select(driver.findElement(By.id("categoryId"))).selectByIndex(0);
            } catch (Exception ignored) {}

            // Click Save
            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add') or contains(text(), 'Thêm')]"));
            smartClick(saveBtn);

            // Chờ xử lý backend
            Thread.sleep(2000);
            driver.navigate().refresh();
            waitForPageLoaded();
            waitForDataTableLoaded(); // QUAN TRỌNG: Chờ DataTables render lại sau khi refresh

            // Tìm kiếm
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchBox.clear();
            searchBox.sendKeys(proName);

            // Chờ DataTables lọc kết quả (JS chạy mất khoảng 500ms-1s)
            Thread.sleep(1500);

            WebElement tableBody = driver.findElement(By.cssSelector("table#add-row tbody"));
            Assert.assertTrue(tableBody.getText().contains(proName), "Không tìm thấy sản phẩm vừa thêm!");

        } catch (Exception e) {
            takeScreenshot("Admin_Product_CRUD_Error");
            Assert.fail("Lỗi Product CRUD: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    void test_order_crud() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/orders");
        waitForPageLoaded();
        waitForDataTableLoaded();

        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table#add-row tbody tr"));
            // Check kỹ hơn: DataTables thường có dòng "No data available in table"
            if (rows.isEmpty() || rows.get(0).getText().contains("No data")) {
                System.out.println("⚠️ Không có đơn hàng để test.");
                return;
            }

            WebElement editLink = rows.get(0).findElement(By.cssSelector("a[href*='editorder']"));
            smartClick(editLink);

            wait.until(ExpectedConditions.urlContains("editorder"));

            Select statusSelect = new Select(driver.findElement(By.name("status")));
            statusSelect.selectByIndex(statusSelect.getOptions().size() - 1);

            WebElement updateBtn = driver.findElement(By.xpath("//button[contains(text(), 'Update')]"));
            smartClick(updateBtn);

            wait.until(ExpectedConditions.urlContains("orders"));

        } catch (Exception e) {
            takeScreenshot("Admin_Order_Error");
            Assert.fail("Lỗi Order: " + e.getMessage());
        }
    }

    @Test(priority = 7)
    void test_add_product_fail_empty_name() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");
        waitForPageLoaded();
        waitForDataTableLoaded();

        try {
            System.out.println("Test 7: Thử thêm sản phẩm thiếu tên...");
            openAddRowModal();

            // Đảm bảo modal đã mở và field price tương tác được
            WebElement priceInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("price")));
            priceInput.clear();
            priceInput.sendKeys("100");

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            saveBtn.click(); // Click thường để trigger HTML5 validation

            Thread.sleep(1000);

            // Verify modal vẫn hiển thị (do chưa submit được)
            WebElement modal = driver.findElement(By.id("addRowModal"));
            Assert.assertTrue(modal.isDisplayed(), "Form bị submit dù thiếu tên!");

        } catch (Exception e) {
            takeScreenshot("Product_EmptyName_Fail");
            Assert.fail("Lỗi test 7: " + e.getMessage());
        }
    }

    @Test(priority = 8)
    void test_add_product_fail_negative_price() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");
        waitForPageLoaded();
        waitForDataTableLoaded();

        try {
            openAddRowModal();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys("Negative Price");
            driver.findElement(By.id("price")).sendKeys("-100");

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            saveBtn.click();

            Thread.sleep(1000);

            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Client validation chặn giá âm.");
            } else {
                driver.navigate().refresh();
                waitForPageLoaded();
                waitForDataTableLoaded();

                WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
                searchBox.sendKeys("Negative Price");
                Thread.sleep(1000);

                Assert.assertFalse(driver.findElement(By.tagName("tbody")).getText().contains("-100"), "Lỗi: Giá âm được lưu!");
            }
        } catch (Exception e) {
            takeScreenshot("Product_NegativePrice_Fail");
            Assert.fail("Lỗi test 8: " + e.getMessage());
        }
    }
}