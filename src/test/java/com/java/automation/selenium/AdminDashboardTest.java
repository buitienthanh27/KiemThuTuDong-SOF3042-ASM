package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
        // Tăng timeout lên 30s để an toàn cho việc load DataTables
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        loginPage = new LoginOrRegisterPage(driver);
    }

    // --- HÀM HỖ TRỢ MỞ MODAL THÔNG MINH (RETRY LOGIC) ---
    // Khắc phục lỗi: no such element: {"method":"css selector","selector":"#addRowModal"}
    private void openAddRowModal() {
        By modalLocator = By.id("addRowModal");
        By btnLocator = By.cssSelector("button[data-target='#addRowModal']");

        waitForPageLoaded(); // Chờ trang load xong script

        // Thử tối đa 3 lần nếu click bị trượt
        for (int i = 0; i < 3; i++) {
            try {
                // 1. Tìm nút
                WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(btnLocator));

                // 2. Click bằng Smart Click (JS + Scroll)
                smartClick(addBtn);

                // 3. Chờ Modal hiện ra trong 3 giây
                new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.visibilityOfElementLocated(modalLocator));

                return; // Thành công thì thoát hàm
            } catch (Exception e) {
                System.out.println("⚠️ Mở modal thất bại lần " + (i + 1) + ", đang thử lại...");
                // Refresh trang để reset trạng thái nút bấm
                driver.navigate().refresh();
                waitForPageLoaded();
            }
        }
        Assert.fail("Lỗi: Không thể mở Modal thêm mới sau 3 lần thử!");
    }

    private void loginAsAdmin() {
        loginPage.navigateToLoginPage();
        if (!loginPage.isOnLoginPage()) {
            if (!driver.getCurrentUrl().contains("admin")) {
                driver.get(TestConfig.getBaseUrl() + "/admin/home");
            }
            return;
        }
        String user = TestConfig.getProperty("admin.username");
        String pass = TestConfig.getProperty("admin.password");
        // Fallback user mặc định nếu config lỗi
        loginPage.login(user == null ? "admin" : user, pass == null ? "123123" : pass);
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
            Assert.fail("Không vào được Dashboard: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    void test_product_crud() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");
        waitForPageLoaded();

        try {
            System.out.println("Test 2.1: Thêm sản phẩm...");
            openAddRowModal(); // Dùng hàm mở modal thông minh

            String proName = "Auto " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(proName);
            driver.findElement(By.id("price")).sendKeys("150");
            driver.findElement(By.id("quantity")).sendKeys("10");
            driver.findElement(By.id("description")).sendKeys("Desc");

            try { new Select(driver.findElement(By.id("categoryId"))).selectByIndex(0); } catch (Exception ignored) {}

            // Click Save
            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add') or contains(text(), 'Thêm')]"));
            smartClick(saveBtn);

            // Chờ bảng reload
            Thread.sleep(2000);
            driver.navigate().refresh();
            waitForPageLoaded();

            // Tìm kiếm để verify
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchBox.sendKeys(proName);
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

        try {
            // Kiểm tra bảng có dữ liệu không
            List<WebElement> rows = driver.findElements(By.cssSelector("table#add-row tbody tr"));
            if (rows.isEmpty() || rows.get(0).getText().contains("No data")) {
                System.out.println("⚠️ Không có đơn hàng để test.");
                return;
            }

            // Tìm nút Edit
            WebElement editLink = rows.get(0).findElement(By.cssSelector("a[href*='editorder']"));
            smartClick(editLink);

            wait.until(ExpectedConditions.urlContains("editorder"));

            // Đổi trạng thái
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

        try {
            System.out.println("Test 7: Thử thêm sản phẩm thiếu tên...");
            openAddRowModal(); // Mở modal (Retry nếu cần)

            driver.findElement(By.id("price")).sendKeys("100");

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            // Click thường để trigger validation HTML5
            saveBtn.click();

            Thread.sleep(1000);

            // Modal vẫn phải hiển thị vì chưa submit được
            Assert.assertTrue(driver.findElement(By.id("addRowModal")).isDisplayed(), "Form bị submit dù thiếu tên!");

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

        try {
            openAddRowModal();
            driver.findElement(By.id("name")).sendKeys("Negative Price");
            driver.findElement(By.id("price")).sendKeys("-100");

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            saveBtn.click();

            Thread.sleep(1000);

            // Check: Modal vẫn còn (Validation Client) HOẶC Lưu không thành công (Server check)
            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Client validation chặn giá âm.");
            } else {
                // Nếu modal tắt, check bảng xem có lưu bậy không
                driver.navigate().refresh();
                waitForPageLoaded();
                WebElement searchBox = driver.findElement(By.cssSelector("div.dataTables_filter input"));
                searchBox.sendKeys("Negative Price");
                Thread.sleep(1000);
                Assert.assertFalse(driver.findElement(By.tagName("tbody")).getText().contains("-100"), "Lỗi: Giá âm được lưu!");
            }
        } catch (Exception e) {
            Assert.fail("Lỗi test 8: " + e.getMessage());
        }
    }

    // Test Supplier và Category tương tự, chỉ cần gọi openAddRowModal() thay vì tự click
    @Test(priority = 4)
    void test_manage_categories() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/categories");
        try {
            openAddRowModal();
            String catName = "Cat " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(catName);
            smartClick(driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")));

            Thread.sleep(2000);
            driver.navigate().refresh();
            waitForPageLoaded();

            driver.findElement(By.cssSelector("div.dataTables_filter input")).sendKeys(catName);
            Thread.sleep(1000);
            Assert.assertTrue(driver.findElement(By.tagName("tbody")).getText().contains(catName));
        } catch (Exception e) {
            Assert.fail("Lỗi Category: " + e.getMessage());
        }
    }
}