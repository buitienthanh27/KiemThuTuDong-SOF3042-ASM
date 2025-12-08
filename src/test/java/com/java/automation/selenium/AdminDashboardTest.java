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

import java.io.File;
import java.time.Duration;
import java.util.List;

@Listeners(TestListener.class)
public class AdminDashboardTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private LoginOrRegisterPage loginPage;

    // Ảnh mẫu để upload
    private static final String IMAGE_PATH = System.getProperty("user.dir") + "/src/main/resources/static/images/product/02.jpg";

    @BeforeMethod
    public void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Tăng wait lên 20s
        loginPage = new LoginOrRegisterPage(driver);
    }

    // --- HÀM HỖ TRỢ ---

    // Click JS mạnh mẽ hơn: Scroll -> Click -> Retry nếu fail
    private void clickJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            // Fallback: Click trực tiếp không cần scroll
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void loginAsAdmin() {
        loginPage.navigateToLoginPage();

        // Nếu chưa ở login (tức là đã login rồi) thì kiểm tra xem có phải admin không
        if (!loginPage.isOnLoginPage()) {
            if (!driver.getCurrentUrl().contains("admin")) {
                driver.get(TestConfig.getBaseUrl() + "/admin/home");
            }
            return;
        }

        String user = TestConfig.getProperty("admin.username");
        String pass = TestConfig.getProperty("admin.password");
        if (user == null) user = "admin";
        if (pass == null) pass = "123123";

        loginPage.login(user, pass);

        // Wait URL chuyển sang admin
        try {
            wait.until(ExpectedConditions.urlContains("admin"));
            System.out.println("✅ Đã vào trang Admin.");
        } catch (Exception e) {
            // Nếu login xong về trang chủ, tự redirect
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
            Assert.fail("Không vào được Dashboard: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    void test_product_crud() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");

        try {
            System.out.println("Test 2.1: Thêm sản phẩm...");

            // 1. Mở Modal (Dùng JS Click để tránh bị che)
            WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-target='#addRowModal']")));
            clickJS(addBtn);

            // 2. Chờ Modal hiện
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500); // Chờ animation

            // 3. Điền form
            String proName = "AutoPro " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(proName);
            driver.findElement(By.id("price")).sendKeys("150");
            driver.findElement(By.id("quantity")).sendKeys("10");
            driver.findElement(By.id("description")).sendKeys("Test Description");

            // Chọn Category (nếu có)
            try {
                new Select(driver.findElement(By.id("categoryId"))).selectByIndex(0);
            } catch (Exception ignored) {}

            // Click Save
            WebElement saveBtn = modal.findElement(By.xpath(".//button[contains(text(), 'Add') or contains(text(), 'Thêm')]"));
            clickJS(saveBtn);

            // 4. Verify Thêm thành công
            // Chờ bảng reload xong (tìm ô search của datatable)
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchBox.clear();
            searchBox.sendKeys(proName);
            Thread.sleep(1500); // Chờ filter

            WebElement tableBody = driver.findElement(By.cssSelector("table#add-row tbody"));
            Assert.assertTrue(tableBody.getText().contains(proName), "Không tìm thấy sản phẩm vừa thêm!");

            // 5. Delete (Dọn dẹp)
            System.out.println("Test 2.2: Xóa sản phẩm vừa tạo...");
            WebElement deleteBtn = tableBody.findElement(By.cssSelector("button[onclick*='showConfigModalDialog']")); // Nút xóa dòng đầu tiên
            clickJS(deleteBtn);

            // Chờ modal xác nhận
            WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("yesOption")));
            clickJS(confirmBtn);

            Thread.sleep(1000); // Chờ xóa xong

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

            // Chờ bảng xuất hiện
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table#add-row")));

            List<WebElement> rows = driver.findElements(By.cssSelector("table#add-row tbody tr"));
            if (rows.isEmpty() || rows.get(0).getText().contains("No data")) {
                System.out.println("⚠️ Không có đơn hàng để test.");
                return;
            }

            // Tìm nút sửa (Edit) ở dòng đầu tiên
            // Dùng css selector linh hoạt hơn
            WebElement editLink = rows.get(0).findElement(By.cssSelector("a[href*='editorder']"));
            clickJS(editLink);

            wait.until(ExpectedConditions.urlContains("editorder"));

            // Sửa trạng thái
            Select statusSelect = new Select(driver.findElement(By.name("status")));
            statusSelect.selectByIndex(statusSelect.getOptions().size() - 1); // Chọn cái cuối (thường là Cancel/Completed)

            WebElement updateBtn = driver.findElement(By.xpath("//button[contains(text(), 'Update') or contains(text(), 'Cập nhật')]"));
            clickJS(updateBtn);

            wait.until(ExpectedConditions.urlContains("orders"));
            System.out.println("✅ Update đơn hàng thành công.");

        } catch (Exception e) {
            takeScreenshot("Admin_Order_Error");
            Assert.fail("Lỗi Order: " + e.getMessage());
        }
    }

    @Test(priority = 4)
    void test_manage_categories() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/categories");

        try {
            System.out.println("Test 4.1: Thêm Category...");
            WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-target='#addRowModal']")));
            clickJS(addBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));

            String catName = "Cat " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(catName);

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            clickJS(saveBtn);

            // Verify
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchBox.sendKeys(catName);
            Thread.sleep(1000);

            String bodyText = driver.findElement(By.tagName("tbody")).getText();
            Assert.assertTrue(bodyText.contains(catName), "Thêm Category thất bại!");

        } catch (Exception e) {
            takeScreenshot("Admin_Category_Error");
            Assert.fail("Lỗi Category: " + e.getMessage());
        }
    }

    @Test(priority = 5)
    void test_manage_suppliers() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/suppliers");

        try {
            System.out.println("Test 5.1: Thêm Supplier...");
            WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-target='#addRowModal']")));
            clickJS(addBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));

            String supName = "Sup " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(supName);
            driver.findElement(By.id("email")).sendKeys("sup" + System.currentTimeMillis() + "@test.com");
            driver.findElement(By.id("phone")).sendKeys("0123456789");

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            clickJS(saveBtn);

            // Verify
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchBox.sendKeys(supName);
            Thread.sleep(1000);

            String bodyText = driver.findElement(By.tagName("tbody")).getText();
            Assert.assertTrue(bodyText.contains(supName), "Thêm Supplier thất bại!");

        } catch (Exception e) {
            Assert.fail("Lỗi Supplier: " + e.getMessage());
        }
    }

    @Test(priority = 6)
    void test_view_customers() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/customers");
        try {
            System.out.println("Test 6: Xem danh sách khách hàng...");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            Assert.assertTrue(searchBox.isDisplayed());
        } catch (Exception e) {
            Assert.fail("Lỗi xem Customer: " + e.getMessage());
        }
    }

    @Test(priority = 7)
    void test_add_product_fail_empty_name() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products");

        try {
            System.out.println("Test 7: Thử thêm sản phẩm nhưng bỏ trống Tên...");
            WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-target='#addRowModal']")));
            clickJS(addBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));

            // Để trống tên, chỉ điền giá
            driver.findElement(By.id("price")).sendKeys("100");

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            // Click thường để kích hoạt HTML5 validation (nếu có)
            saveBtn.click();

            Thread.sleep(1000);

            // Check nếu modal vẫn còn hiện -> Pass (nghĩa là chưa submit được)
            boolean isModalVisible = driver.findElement(By.id("addRowModal")).isDisplayed();
            Assert.assertTrue(isModalVisible, "Lỗi: Form submit thành công dù thiếu tên!");

        } catch (Exception e) {
            Assert.fail("Lỗi test 7: " + e.getMessage());
        }
    }

    @Test(priority = 8)
    void test_add_product_fail_negative_price() {
        loginAsAdmin();
        driver.get(TestConfig.getBaseUrl() + "/admin/products"); // Refresh lại trang

        try {
            System.out.println("Test 8: Thử thêm sản phẩm giá âm...");
            WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-target='#addRowModal']")));
            clickJS(addBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));

            driver.findElement(By.id("name")).sendKeys("Price Negative Test");
            driver.findElement(By.id("price")).sendKeys("-500"); // Giá âm

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            saveBtn.click();

            Thread.sleep(1000);

            // Check 1: Modal còn đó (Validation client chặn) -> Pass
            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Hệ thống chặn giá âm.");
                return;
            }

            // Check 2: Nếu modal tắt, kiểm tra xem có lưu vào bảng không (Validation server chặn)
            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchBox.clear();
            searchBox.sendKeys("Price Negative Test");
            Thread.sleep(1000);

            String bodyText = driver.findElement(By.tagName("tbody")).getText();
            if (bodyText.contains("Price Negative Test")) {
                // Nếu tìm thấy -> Fail (đã lưu được)
                Assert.fail("LỖI: Hệ thống cho phép lưu sản phẩm giá âm!");
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
            System.out.println("Test 9: Thêm NCC email sai...");
            WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-target='#addRowModal']")));
            clickJS(addBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));

            driver.findElement(By.id("name")).sendKeys("Bad Email");
            driver.findElement(By.id("email")).sendKeys("email_khong_hop_le");

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            saveBtn.click();

            Thread.sleep(1000);

            Assert.assertTrue(driver.findElement(By.id("addRowModal")).isDisplayed(), "Lỗi: Email sai định dạng vẫn submit được!");

        } catch (Exception e) {
            Assert.fail("Lỗi test 9: " + e.getMessage());
        }
    }
}