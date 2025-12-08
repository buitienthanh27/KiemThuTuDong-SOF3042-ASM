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

// Kết nối với TestListener để chụp ảnh lỗi tự động
@Listeners(TestListener.class)
public class AdminDashboardTest extends BaseSeleniumTest {

    private WebDriverWait wait;

    // Đường dẫn ảnh tương thích mọi hệ điều hành (Windows/Linux/Mac)
    private static final String IMAGE_PATH = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "images" + File.separator + "product" + File.separator + "02.jpg";

    @BeforeMethod
    void setUpTest() {
        // Tăng timeout lên 30s cho môi trường CI chậm
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // --- HELPER METHODS ---

    /**
     * Hàm đăng nhập Admin chuẩn (Sử dụng Page Object & Config)
     * Được gọi ở đầu mỗi Test Case để đảm bảo quyền truy cập.
     */
    public void loginAsAdmin() {
        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();

        // Lấy tài khoản Admin từ file config (test.properties)
        // Đảm bảo trong test.properties bạn đã set: test.admin.id=admin và test.admin.password=123123
        String adminUser = TestConfig.getProperty("test.admin.id");
        String adminPass = TestConfig.getProperty("test.admin.password");

        System.out.println("🔄 Đang đăng nhập Admin: " + adminUser);
        loginPage.login(adminUser, adminPass);

        // Chờ vào được trang Admin (hoặc trang chủ nếu redirect)
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("admin"),
                    ExpectedConditions.urlContains("home"),
                    ExpectedConditions.urlToBe(BASE_URL)
            ));

            // Nếu login xong mà chưa vào admin (về home), ép chuyển hướng vào trang dashboard
            if (!driver.getCurrentUrl().contains("admin")) {
                driver.get(BASE_URL + "admin/home");
            }
            System.out.println("✅ Đã vào trang Admin.");
        } catch (Exception e) {
            Assert.fail("Login Admin thất bại! Vẫn kẹt ở: " + driver.getCurrentUrl());
        }
    }

    // Hàm click an toàn bằng Javascript (Tránh lỗi element not clickable trên CI)
    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    // --- TEST CASES ---

    @Test(priority = 1)
    void test_access_admin_dashboard() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI

        driver.get(BASE_URL + "admin/home");
        try {
            WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(text(), 'Dashboard')]")
            ));
            Assert.assertTrue(dashboardTitle.isDisplayed());
        } catch (Exception e) {
            Assert.fail("Lỗi truy cập Admin: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    void test_product_crud() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI
        driver.get(BASE_URL + "admin/products");

        try {
            // 1. CREATE
            System.out.println("Test 2.1: Thêm sản phẩm...");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']")));
            clickElementJS(addBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500); // Chờ modal ổn định

            String productName = "AutoPro " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(productName);

            // Chọn Category & Supplier (nếu có)
            try {
                new Select(driver.findElement(By.id("categoryId"))).selectByIndex(0);
                new Select(driver.findElement(By.id("supplierId"))).selectByIndex(0);
            } catch (Exception ignored) {}

            driver.findElement(By.id("price")).sendKeys("100");
            driver.findElement(By.id("quantity")).sendKeys("10");
            driver.findElement(By.id("discount")).sendKeys("0");

            // Upload ảnh (nếu file tồn tại)
            try {
                File img = new File(IMAGE_PATH);
                if (img.exists()) {
                    driver.findElement(By.id("image")).sendKeys(img.getAbsolutePath());
                } else {
                    System.out.println("⚠️ Không tìm thấy ảnh test: " + IMAGE_PATH);
                }
            } catch (Exception ignored) {}

            driver.findElement(By.id("description")).sendKeys("Desc Auto");

            // Click Save
            clickElementJS(driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")));

            // Verify Create
            Thread.sleep(2000);
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.clear();
            searchInput.sendKeys(productName);
            Thread.sleep(1000);

            if (!driver.findElement(By.id("add-row")).getText().toLowerCase().contains(productName.toLowerCase())) {
                Assert.fail("Lỗi: Thêm sản phẩm thất bại (Không tìm thấy tên trong bảng).");
            }

            // 2. UPDATE
            System.out.println("Test 2.2: Sửa sản phẩm...");
            clickElementJS(driver.findElement(By.cssSelector("a[href*='editProduct']")));

            WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@id='name' and not(@readonly)]")
            ));
            nameInput.clear();

            String updatedName = productName + " Up";
            nameInput.sendKeys(updatedName);

            clickElementJS(driver.findElement(By.xpath("//button[contains(text(), 'Update')]")));

            Thread.sleep(1000);
            if (!driver.getCurrentUrl().contains("products")) {
                driver.get(BASE_URL + "admin/products");
            }

            searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.clear();
            searchInput.sendKeys(updatedName);
            Thread.sleep(1000);

            if (!driver.findElement(By.id("add-row")).getText().toLowerCase().contains(updatedName.toLowerCase())) {
                Assert.fail("Lỗi: Sửa sản phẩm thất bại.");
            }

            // 3. DELETE
            System.out.println("Test 2.3: Xóa sản phẩm...");
            clickElementJS(driver.findElement(By.cssSelector("button[onclick*='showConfigModalDialog']")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));
            Thread.sleep(500);
            clickElementJS(driver.findElement(By.id("yesOption")));

            Thread.sleep(1500);
            searchInput = driver.findElement(By.cssSelector("div.dataTables_filter input"));
            searchInput.clear();
            searchInput.sendKeys(updatedName);
            Thread.sleep(1000);

            String tableText = driver.findElement(By.id("add-row")).getText();
            if (!tableText.contains("No matching") && tableText.toLowerCase().contains(updatedName.toLowerCase())) {
                Assert.fail("Lỗi: Xóa sản phẩm thất bại.");
            }

        } catch (Exception e) {
            Assert.fail("Lỗi Product CRUD: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    void test_order_crud() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI
        driver.get(BASE_URL + "admin/orders");
        try {
            System.out.println("Test 3: Quản lý đơn hàng...");
            // Check nếu bảng rỗng thì bỏ qua
            if (driver.findElements(By.cssSelector("table#add-row tbody tr")).isEmpty()) return;

            // Edit Status
            clickElementJS(driver.findElement(By.cssSelector("table#add-row tbody tr a[href*='editorder']")));
            wait.until(ExpectedConditions.urlContains("editorder"));

            Select statusSelect = new Select(driver.findElement(By.name("status")));
            // Chọn trạng thái cuối cùng
            statusSelect.selectByIndex(statusSelect.getOptions().size() - 1);

            clickElementJS(driver.findElement(By.xpath("//button[contains(text(), 'Update')]")));

            Thread.sleep(1000);
            if (!driver.getCurrentUrl().contains("orders")) {
                driver.get(BASE_URL + "admin/orders");
            }

            // Delete
            if(!driver.findElements(By.cssSelector("table#add-row tbody tr")).isEmpty()) {
                clickElementJS(driver.findElement(By.cssSelector("table#add-row tbody tr button[onclick*='showConfigModalDialog']")));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));
                Thread.sleep(500);
                clickElementJS(driver.findElement(By.id("yesOption")));
                Thread.sleep(1500);
                System.out.println("⚠️ Đã thực hiện xóa đơn hàng.");
            }

        } catch (Exception e) {
            Assert.fail("Lỗi Order: " + e.getMessage());
        }
    }

    @Test(priority = 4)
    void test_manage_categories() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI
        driver.get(BASE_URL + "admin/categories");
        try {
            System.out.println("Test 4.1: Thêm Category...");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']")));
            clickElementJS(addBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            String catName = "Cat " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(catName);
            clickElementJS(driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")));

            Thread.sleep(1500);
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.sendKeys(catName);
            Thread.sleep(1000);

            if (!driver.findElement(By.id("add-row")).getText().toLowerCase().contains(catName.toLowerCase())) {
                Assert.fail("Lỗi: Thêm Category thất bại.");
            }

            // Edit & Delete (nếu cần thêm logic ở đây)

        } catch (Exception e) {
            Assert.fail("Lỗi Category: " + e.getMessage());
        }
    }

    @Test(priority = 5)
    void test_manage_suppliers() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI
        driver.get(BASE_URL + "admin/suppliers");
        try {
            System.out.println("Test 5.1: Thêm Supplier...");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']")));
            clickElementJS(addBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            String supName = "Sup " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(supName);
            driver.findElement(By.id("email")).sendKeys("sup@test.com");
            driver.findElement(By.id("phone")).sendKeys("123");
            clickElementJS(driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")));

            Thread.sleep(1500);
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.sendKeys(supName);
            Thread.sleep(500);

            if (!driver.findElement(By.id("add-row")).getText().toLowerCase().contains(supName.toLowerCase())) {
                Assert.fail("Lỗi: Thêm Supplier thất bại.");
            }
        } catch (Exception e) {
            Assert.fail("Lỗi Supplier: " + e.getMessage());
        }
    }

    @Test(priority = 6)
    void test_view_customers() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI
        driver.get(BASE_URL + "admin/customers");
        try {
            System.out.println("Test 6: Xem danh sách khách hàng...");
            WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-row")));
            Assert.assertTrue(table.isDisplayed());

            driver.findElement(By.cssSelector("div.dataTables_filter input")).sendKeys("admin");
            Thread.sleep(500);
        } catch (Exception e) {
            Assert.fail("Lỗi Customer: " + e.getMessage());
        }
    }

    @Test(priority = 7)
    void test_add_product_fail_empty_name() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI
        driver.get(BASE_URL + "admin/products");

        try {
            System.out.println("Test 7: Thử thêm sản phẩm nhưng bỏ trống Tên...");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']")));
            clickElementJS(addBtn);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            driver.findElement(By.id("name")).clear();
            driver.findElement(By.id("price")).sendKeys("100");
            driver.findElement(By.id("quantity")).sendKeys("10");

            WebElement submitBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            submitBtn.click(); // Click thường để trigger validation

            Thread.sleep(1000);

            // Nếu modal vẫn hiện -> Pass
            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Bị chặn.");
            } else {
                Assert.fail("Lỗi: Hệ thống không chặn!");
            }

        } catch (Exception e) {
            Assert.fail("Lỗi test case 7: " + e.getMessage());
        }
    }

    @Test(priority = 8)
    void test_add_product_fail_negative_price() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI
        // Refresh hoặc vào lại trang để đảm bảo sạch sẽ
        driver.get(BASE_URL + "admin/products");

        try {
            System.out.println("Test 8: Thử thêm sản phẩm giá âm...");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']")));
            clickElementJS(addBtn);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            driver.findElement(By.id("name")).sendKeys("Negative Price");
            driver.findElement(By.id("price")).sendKeys("-500");
            driver.findElement(By.id("quantity")).sendKeys("10");

            WebElement submitBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            submitBtn.click();

            Thread.sleep(1000);

            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Hệ thống chặn giá âm.");
            } else {
                WebElement searchInput = driver.findElement(By.cssSelector("div.dataTables_filter input"));
                searchInput.sendKeys("Negative Price");
                Thread.sleep(1000);
                if(driver.findElement(By.id("add-row")).getText().contains("-500")) {
                    Assert.fail("LỖI: Giá âm được chấp nhận!");
                }
            }

        } catch (Exception e) {
            Assert.fail("Lỗi test 8: " + e.getMessage());
        }
    }

    @Test(priority = 9)
    void test_add_supplier_fail_invalid_email() {
        loginAsAdmin(); // BẮT BUỘC PHẢI GỌI
        driver.get(BASE_URL + "admin/suppliers");

        try {
            System.out.println("Test 9: Thêm NCC email sai...");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']")));
            clickElementJS(addBtn);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            driver.findElement(By.id("name")).sendKeys("Bad Email Supplier");
            driver.findElement(By.id("email")).sendKeys("email_sai");
            driver.findElement(By.id("phone")).sendKeys("0999");

            WebElement submitBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            submitBtn.click();

            Thread.sleep(1000);

            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Bị chặn.");
            } else {
                Assert.fail("Lỗi: Email sai vẫn lưu được!");
            }

        } catch (Exception e) {
            Assert.fail("Lỗi test 9: " + e.getMessage());
        }
    }
}