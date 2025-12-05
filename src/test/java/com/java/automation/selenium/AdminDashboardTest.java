package com.java.automation.selenium;

import com.java.automation.selenium.BaseSeleniumTest;
import com.java.automation.selenium.TestListener;
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
    private static final int TIMEOUT = 10;

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "123123";
    private static final String IMAGE_PATH = System.getProperty("user.dir") + "/src/main/resources/static/images/product/02.jpg";

    @BeforeMethod
    void setUp() {
        // Driver đã có sẵn từ BaseSeleniumTest
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    private void loginAsAdmin() {
        driver.get(BASE_URL + "login");
        try {
            if (!driver.getCurrentUrl().contains("login")) {
                driver.get(BASE_URL + "logout");
                driver.get(BASE_URL + "login");
            }
            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            userField.sendKeys(ADMIN_USER);
            driver.findElement(By.name("password")).sendKeys(ADMIN_PASS);

            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            clickElementJS(loginBtn);

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe(BASE_URL),
                    ExpectedConditions.urlContains("admin")
            ));
        } catch (Exception e) {
            System.out.println("Login Admin Note: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    void test_access_admin_dashboard() {
        loginAsAdmin();
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
        driver.get(BASE_URL + "admin/products");

        try {
            // 1. CREATE
            System.out.println("Test 2.1: Thêm sản phẩm...");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']")));
            clickElementJS(addBtn);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            String productName = "AutoPro " + System.currentTimeMillis();
            driver.findElement(By.id("name")).sendKeys(productName);

            try {
                new Select(driver.findElement(By.id("categoryId"))).selectByIndex(0);
                new Select(driver.findElement(By.id("supplierId"))).selectByIndex(0);
            } catch (Exception ignored) {}

            driver.findElement(By.id("price")).sendKeys("100");
            driver.findElement(By.id("quantity")).sendKeys("10");
            driver.findElement(By.id("discount")).sendKeys("0");

            try {
                File img = new File(IMAGE_PATH);
                if (img.exists()) driver.findElement(By.id("image")).sendKeys(IMAGE_PATH);
            } catch (Exception ignored) {}

            driver.findElement(By.id("description")).sendKeys("Desc");
            clickElementJS(driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]")));

            // Check Create
            Thread.sleep(2000);
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.clear();
            searchInput.sendKeys(productName);
            Thread.sleep(1000);

            if (!driver.findElement(By.id("add-row")).getText().toLowerCase().contains(productName.toLowerCase())) {
                Assert.fail("Lỗi: Thêm sản phẩm thất bại.");
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
        driver.get(BASE_URL + "admin/orders");
        try {
            System.out.println("Test 3: Quản lý đơn hàng...");
            // Check nếu bảng rỗng thì bỏ qua
            if (driver.findElements(By.cssSelector("table#add-row tbody tr")).isEmpty()) return;

            // Edit Status
            clickElementJS(driver.findElement(By.cssSelector("table#add-row tbody tr a[href*='editorder']")));
            wait.until(ExpectedConditions.urlContains("editorder"));

            Select statusSelect = new Select(driver.findElement(By.name("status")));
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
        driver.get(BASE_URL + "admin/categories");
        try {
            // Add
            System.out.println("Test 4.1: Thêm Category...");
            clickElementJS(wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']"))));
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

            // Edit
            System.out.println("Test 4.2: Sửa Category...");
            clickElementJS(driver.findElement(By.cssSelector("a[href*='editCategory']")));

            WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
            nameInput.clear();
            String catUpdate = catName + " Up";
            nameInput.sendKeys(catUpdate);
            clickElementJS(driver.findElement(By.cssSelector("button[type='submit']")));

            Thread.sleep(1000);
            if (!driver.getCurrentUrl().contains("categories")) {
                driver.get(BASE_URL + "admin/categories");
            }

            // Delete
            System.out.println("Test 4.3: Xóa Category...");
            searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.clear();
            searchInput.sendKeys(catUpdate);
            Thread.sleep(1000);

            if(!driver.findElement(By.id("add-row")).getText().contains("No matching")) {
                clickElementJS(driver.findElement(By.cssSelector("button[onclick*='showConfigModalDialog']")));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));
                Thread.sleep(500);
                clickElementJS(driver.findElement(By.id("yesOption")));
                Thread.sleep(1500);
            }

        } catch (Exception e) {
            Assert.fail("Lỗi Category: " + e.getMessage());
        }
    }

    @Test(priority = 5)
    void test_manage_suppliers() {
        driver.get(BASE_URL + "admin/suppliers");
        try {
            // Add
            System.out.println("Test 5.1: Thêm Supplier...");
            clickElementJS(wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']"))));
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

            // Edit
            System.out.println("Test 5.2: Sửa Supplier...");
            clickElementJS(driver.findElement(By.cssSelector("a[href*='editSupplier']")));

            WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
            emailInput.clear();
            emailInput.sendKeys("up@test.com");
            clickElementJS(driver.findElement(By.cssSelector("button[type='submit']")));

            Thread.sleep(1000);
            if (!driver.getCurrentUrl().contains("suppliers")) {
                driver.get(BASE_URL + "admin/suppliers");
            }

            // Delete
            System.out.println("Test 5.3: Xóa Supplier...");
            searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.clear();
            searchInput.sendKeys(supName);
            Thread.sleep(1000);

            clickElementJS(driver.findElement(By.cssSelector("button[onclick*='showConfigModalDialog']")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));
            Thread.sleep(500);
            clickElementJS(driver.findElement(By.id("yesOption")));
            Thread.sleep(1500);

        } catch (Exception e) {
            Assert.fail("Lỗi Supplier: " + e.getMessage());
        }
    }

    @Test(priority = 6)
    void test_view_customers() {
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
                takeScreenshot("Error_Product_EmptyName_Failed");
                Assert.fail("Lỗi: Hệ thống không chặn!");
            }

        } catch (Exception e) {
            Assert.fail("Lỗi test case 7: " + e.getMessage());
        }
    }

    @Test(priority = 8)
    void test_add_product_fail_negative_price() {
        driver.navigate().refresh();
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
                takeScreenshot("Error_Product_NegativePrice_Failed");
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
                takeScreenshot("Error_Supplier_BadEmail_Failed");
                Assert.fail("Lỗi: Email sai vẫn lưu được!");
            }

        } catch (Exception e) {
            Assert.fail("Lỗi test 9: " + e.getMessage());
        }
    }
}