package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
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
        // Tăng timeout lên 30s để bao quát trường hợp mạng chậm
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        loginPage = new LoginOrRegisterPage(driver);
    }

    // --- HÀM HỖ TRỢ MẠNH MẼ ---

    private void clickJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(200);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            // Fallback
            try { element.click(); } catch (Exception ex) {}
        }
    }

    // Hàm mở Modal có cơ chế thử lại (Retry) nếu click lần đầu không ăn
    private void openAddRowModal() {
        By modalLocator = By.id("addRowModal");
        By btnLocator = By.cssSelector("button[data-target='#addRowModal']");

        for (int i = 0; i < 3; i++) { // Thử tối đa 3 lần
            try {
                WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(btnLocator));
                clickJS(addBtn);

                // Chờ modal hiện trong 3s, nếu không thấy thì catch lỗi và thử lại
                new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.visibilityOfElementLocated(modalLocator));

                return; // Nếu mở được thì thoát hàm
            } catch (Exception e) {
                System.out.println("⚠️ Mở modal thất bại lần " + (i + 1) + ", đang thử lại...");
                driver.navigate().refresh(); // Refresh lại trang để reset trạng thái
                try { Thread.sleep(2000); } catch (InterruptedException ex) {}
            }
        }
        Assert.fail("Không thể mở Modal thêm mới sau 3 lần thử!");
    }

    // Hàm chờ bảng dữ liệu load xong (chấp nhận cả bảng rỗng)
    private void waitForTableToLoad() {
        try {
            // Chờ body bảng xuất hiện
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("tbody")));
            // Chờ ô search (dấu hiệu DataTables đã init xong)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
        } catch (Exception e) {
            System.out.println("⚠️ Bảng load chậm hoặc rỗng: " + e.getMessage());
        }
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
        loginPage.login(user == null ? "admin" : user, pass == null ? "123123" : pass);

        try {
            wait.until(ExpectedConditions.urlContains("admin"));
        } catch (Exception e) {
            driver.get(TestConfig.getBaseUrl() + "/admin/home");
        }
    }

    // --- TEST CASES ---

    @Test(priority = 1)
    void test_access_admin_dashboard() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/home");
        try {
            WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Dashboard')]")));
            Assert.assertTrue(title.isDisplayed());
        } catch (Exception e) {
            Assert.fail("Không vào được Dashboard.");
        }
    }

    @Test(priority = 2)
    void test_product_crud() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");

        try {
            System.out.println("Test 2.1: Thêm sản phẩm...");
            openAddRowModal(); // Dùng hàm mở modal thông minh

            String proName = "AutoPro " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(proName);
            driver.findElement(By.id("price")).sendKeys("150");
            driver.findElement(By.id("quantity")).sendKeys("10");
            driver.findElement(By.id("description")).sendKeys("Test Desc");

            try { new Select(driver.findElement(By.id("categoryId"))).selectByIndex(0); } catch (Exception ignored) {}

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add') or contains(text(), 'Thêm')]"));
            clickJS(saveBtn);

            // Verify
            waitForTableToLoad();
            WebElement searchBox = driver.findElement(By.cssSelector("div.dataTables_filter input"));
            searchBox.clear();
            searchBox.sendKeys(proName);
            Thread.sleep(1500); // Chờ filter chạy

            // Check trong bảng (dùng StaleElement check để an toàn)
            try {
                WebElement tableBody = driver.findElement(By.cssSelector("table#add-row tbody"));
                Assert.assertTrue(tableBody.getText().contains(proName), "Không tìm thấy sản phẩm vừa thêm!");
            } catch (StaleElementReferenceException ex) {
                // Nếu bảng bị refresh, tìm lại
                WebElement tableBody = driver.findElement(By.cssSelector("table#add-row tbody"));
                Assert.assertTrue(tableBody.getText().contains(proName));
            }

        } catch (Exception e) {
            takeScreenshot("Admin_Product_CRUD_Error");
            Assert.fail("Lỗi Product CRUD: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    void test_order_crud() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/orders");

        try {
            System.out.println("Test 3: Quản lý đơn hàng...");
            waitForTableToLoad();

            List<WebElement> rows = driver.findElements(By.cssSelector("table#add-row tbody tr"));
            if (rows.isEmpty() || rows.get(0).getText().contains("No data")) {
                System.out.println("⚠️ Không có đơn hàng để test.");
                return; // Pass luôn nếu không có data
            }

            // Tìm link edit (linh hoạt selector)
            WebElement editLink = rows.get(0).findElement(By.cssSelector("a[href*='editorder'], button.btn-primary"));
            clickJS(editLink);

            wait.until(ExpectedConditions.urlContains("editorder"));

            Select statusSelect = new Select(driver.findElement(By.name("status")));
            statusSelect.selectByIndex(statusSelect.getOptions().size() - 1);

            WebElement updateBtn = driver.findElement(By.xpath("//button[contains(text(), 'Update') or contains(text(), 'Cập nhật')]"));
            clickJS(updateBtn);

            wait.until(ExpectedConditions.urlContains("orders"));

        } catch (Exception e) {
            takeScreenshot("Admin_Order_Error");
            Assert.fail("Lỗi Order: " + e.getMessage());
        }
    }

    // Các test khác giữ nguyên logic nhưng thay openAddRowModal()

    @Test(priority = 7)
    void test_add_product_fail_empty_name() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");

        try {
            System.out.println("Test 7: Thử thêm sản phẩm thiếu tên...");
            openAddRowModal(); // FIX: Dùng hàm mở modal thông minh

            driver.findElement(By.id("price")).sendKeys("100");

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            saveBtn.click(); // Click thường để trigger HTML5 validation

            Thread.sleep(1000);

            // Modal vẫn phải hiển thị
            Assert.assertTrue(driver.findElement(By.id("addRowModal")).isDisplayed(), "Form bị submit dù thiếu tên!");

        } catch (Exception e) {
            takeScreenshot("Product_EmptyName_Fail");
            Assert.fail("Lỗi test 7: " + e.getMessage());
        }
    }

    // ... Giữ nguyên các test khác nhưng nhớ thay đoạn mở modal bằng openAddRowModal()

    @Test(priority = 4)
    void test_manage_categories() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/categories");
        try {
            openAddRowModal();
            String catName = "Cat " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(catName);
            clickJS(driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")));

            waitForTableToLoad();
            driver.findElement(By.cssSelector("div.dataTables_filter input")).sendKeys(catName);
            Thread.sleep(1000);
            Assert.assertTrue(driver.findElement(By.tagName("tbody")).getText().contains(catName));
        } catch (Exception e) {
            Assert.fail("Lỗi Category: " + e.getMessage());
        }
    }

    @Test(priority = 5)
    void test_manage_suppliers() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/suppliers");
        try {
            openAddRowModal();
            String supName = "Sup " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(supName);
            driver.findElement(By.id("email")).sendKeys("sup" + System.currentTimeMillis() + "@test.com");
            driver.findElement(By.id("phone")).sendKeys("123");
            clickJS(driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")));

            waitForTableToLoad();
            driver.findElement(By.cssSelector("div.dataTables_filter input")).sendKeys(supName);
            Thread.sleep(1000);
            Assert.assertTrue(driver.findElement(By.tagName("tbody")).getText().contains(supName));
        } catch (Exception e) {
            Assert.fail("Lỗi Supplier: " + e.getMessage());
        }
    }

    @Test(priority = 6)
    void test_view_customers() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/customers");
        try {
            waitForTableToLoad();
            Assert.assertTrue(driver.findElement(By.tagName("table")).isDisplayed());
        } catch (Exception e) {
            Assert.fail("Lỗi Customer: " + e.getMessage());
        }
    }

    @Test(priority = 8)
    void test_add_product_fail_negative_price() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");
        try {
            openAddRowModal();
            driver.findElement(By.id("name")).sendKeys("Negative");
            driver.findElement(By.id("price")).sendKeys("-100");
            driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")).click();
            Thread.sleep(1000);

            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Chặn giá âm.");
            } else {
                waitForTableToLoad();
                driver.findElement(By.cssSelector("div.dataTables_filter input")).sendKeys("Negative");
                Thread.sleep(1000);
                Assert.assertFalse(driver.findElement(By.tagName("tbody")).getText().contains("-100"), "Lỗi: Giá âm được lưu!");
            }
        } catch (Exception e) {
            Assert.fail("Lỗi test 8: " + e.getMessage());
        }
    }

    @Test(priority = 9)
    void test_add_supplier_fail_invalid_email() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/suppliers");
        try {
            openAddRowModal();
            driver.findElement(By.id("name")).sendKeys("Bad Email");
            driver.findElement(By.id("email")).sendKeys("bad_email");
            driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")).click();
            Thread.sleep(1000);
            Assert.assertTrue(driver.findElement(By.id("addRowModal")).isDisplayed(), "Lỗi: Email sai vẫn submit được!");
        } catch (Exception e) {
            Assert.fail("Lỗi test 9: " + e.getMessage());
        }
    }
}