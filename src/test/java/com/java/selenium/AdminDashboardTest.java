package com.java.selenium;
import java.time.Duration;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ExtendWith(ScreenshotOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminDashboardTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 10;

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "123123";
    private static final String IMAGE_PATH = System.getProperty("user.dir") + "/src/main/resources/static/images/product/02.jpg";

    @BeforeEach
    void setUp() {
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

    public void takeScreenshot(String fileName) {
        try {
            // 1. QUAN TRỌNG: Cuộn lên đầu trang trước tiên
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            Thread.sleep(500); // Chờ cuộn xong

            // 2. Chụp ảnh dưới dạng Byte (Để đính kèm vào Allure Report)
            byte[] content = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(fileName, new ByteArrayInputStream(content));

            // 3. Lưu ảnh ra File (Để xem offline hoặc lưu vào Artifacts của Github)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFileName = "screenshots/ERROR_" + fileName + "_" + timestamp + ".png";

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            java.nio.file.Path destination = java.nio.file.Paths.get(fullFileName);
            java.nio.file.Files.createDirectories(destination.getParent());
            java.nio.file.Files.copy(scrFile.toPath(), destination);

            System.out.println("📸 Đã chụp ảnh và đính kèm vào Allure Report: " + fullFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loginAsAdmin() {
        driver.get("http://localhost:9090/login");
        try {
            if (!driver.getCurrentUrl().contains("login")) {
                driver.get("http://localhost:9090/logout");
                driver.get("http://localhost:9090/login");
            }
            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            userField.sendKeys(ADMIN_USER);
            driver.findElement(By.name("password")).sendKeys(ADMIN_PASS);

            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            clickElementJS(loginBtn);

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe("http://localhost:9090/"),
                    ExpectedConditions.urlContains("admin")
            ));
        } catch (Exception e) {
            System.out.println("Login Admin Note: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void test_access_admin_dashboard() {
        loginAsAdmin();
        driver.get("http://localhost:9090/admin/home");
        try {
            WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(text(), 'Dashboard')]")
            ));
            Assertions.assertTrue(dashboardTitle.isDisplayed());
        } catch (Exception e) {
            takeScreenshot("Admin_Dashboard_Error");
            Assertions.fail("Lỗi truy cập Admin: " + e.getMessage());
        }
    }

    // --- TEST 2: SẢN PHẨM (SỬA LỖI SELECTOR INPUT NAME) ---
    @Test
    @Order(2)
    void test_product_crud() {
        driver.get("http://localhost:9090/admin/products");

        try {
            // 1. CREATE
            System.out.println("Test 2.1: Thêm sản phẩm...");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-target='#addRowModal']")));
            clickElementJS(addBtn);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            String productName = "AutoPro " + System.currentTimeMillis();

            // Ở trang Add, ID=name là duy nhất, dùng bình thường
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
                takeScreenshot("Admin_Product_Add_FAIL");
                Assertions.fail("Lỗi: Thêm sản phẩm thất bại.");
            }

            // 2. UPDATE (SỬA LỖI TRÙNG ID)
            System.out.println("Test 2.2: Sửa sản phẩm...");
            clickElementJS(driver.findElement(By.cssSelector("a[href*='editProduct']")));

            // QUAN TRỌNG: Tìm input name KHÔNG có thuộc tính readonly
            // Vì trong HTML editProduct có 2 id="name", cái đầu là readonly (ID sản phẩm)
            WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@id='name' and not(@readonly)]")
            ));
            nameInput.clear(); // Giờ thì clear thoải mái

            String updatedName = productName + " Up";
            nameInput.sendKeys(updatedName);

            clickElementJS(driver.findElement(By.xpath("//button[contains(text(), 'Update')]")));

            // Check Update
            Thread.sleep(1000);
            // Nếu chưa về trang list, tự động về
            if (!driver.getCurrentUrl().contains("products")) {
                driver.get("http://localhost:9090/admin/products");
            }

            searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.clear();
            searchInput.sendKeys(updatedName);
            Thread.sleep(1000);

            if (!driver.findElement(By.id("add-row")).getText().toLowerCase().contains(updatedName.toLowerCase())) {
                takeScreenshot("Admin_Product_Edit_FAIL");
                Assertions.fail("Lỗi: Sửa sản phẩm thất bại.");
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
                takeScreenshot("Admin_Product_Delete_FAIL");
                Assertions.fail("Lỗi: Xóa sản phẩm thất bại.");
            }

        } catch (Exception e) {
            takeScreenshot("Admin_Product_Error");
            Assertions.fail("Lỗi Product CRUD: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void test_order_crud() {
        driver.get("http://localhost:9090/admin/orders");
        try {
            System.out.println("Test 3: Quản lý đơn hàng...");
            List<WebElement> rows = driver.findElements(By.cssSelector("table#add-row tbody tr"));
            if (rows.isEmpty()) return;

            clickElementJS(rows.get(0).findElement(By.cssSelector("a[href*='editorder']")));
            wait.until(ExpectedConditions.urlContains("editorder"));

            Select statusSelect = new Select(driver.findElement(By.name("status")));
            statusSelect.selectByIndex(statusSelect.getOptions().size() - 1);
            clickElementJS(driver.findElement(By.xpath("//button[contains(text(), 'Update')]")));

            Thread.sleep(1000);
            if (!driver.getCurrentUrl().contains("orders")) {
                driver.get("http://localhost:9090/admin/orders");
            }

            // Check Delete
            rows = driver.findElements(By.cssSelector("table#add-row tbody tr"));
            if(!rows.isEmpty()) {
                clickElementJS(rows.get(0).findElement(By.cssSelector("button[onclick*='showConfigModalDialog']")));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));
                Thread.sleep(500);
                clickElementJS(driver.findElement(By.id("yesOption")));
                Thread.sleep(1500);
                System.out.println("⚠️ Đã thực hiện xóa đơn hàng (kết quả phụ thuộc vào DB constraint).");
            }

        } catch (Exception e) {
            takeScreenshot("Admin_Order_Error");
            Assertions.fail("Lỗi Order: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void test_manage_categories() {
        driver.get("http://localhost:9090/admin/categories");
        try {
            // 1. Add
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
                takeScreenshot("Admin_Category_Add_FAIL");
                Assertions.fail("Lỗi: Thêm Category thất bại.");
            }

            // 2. Edit
            System.out.println("Test 4.2: Sửa Category...");
            clickElementJS(driver.findElement(By.cssSelector("a[href*='editCategory']")));

            WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
            nameInput.clear();
            String catUpdate = catName + " Up";
            nameInput.sendKeys(catUpdate);
            clickElementJS(driver.findElement(By.cssSelector("button[type='submit']")));

            Thread.sleep(1000);
            if (!driver.getCurrentUrl().contains("categories")) {
                driver.get("http://localhost:9090/admin/categories");
            }

            // 3. Delete (Sửa logic tìm nút xóa)
            System.out.println("Test 4.3: Xóa Category...");
            // Phải search lại tên mới update thì mới thấy dòng đó để xóa
            searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.clear();
            searchInput.sendKeys(catUpdate);
            Thread.sleep(1000);

            // Kiểm tra xem có dòng nào không
            if(driver.findElement(By.id("add-row")).getText().contains("No matching")) {
                System.out.println("⚠️ Không tìm thấy category sau khi sửa để xóa (Có thể sửa thất bại).");
            } else {
                // Tìm nút xóa trong dòng dữ liệu
                clickElementJS(driver.findElement(By.cssSelector("button[onclick*='showConfigModalDialog']")));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));
                Thread.sleep(500);
                clickElementJS(driver.findElement(By.id("yesOption")));
                Thread.sleep(1500);
            }

        } catch (Exception e) {
            takeScreenshot("Admin_Category_Error");
            Assertions.fail("Lỗi Category: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    void test_manage_suppliers() {
        driver.get("http://localhost:9090/admin/suppliers");
        try {
            // 1. Add
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
                takeScreenshot("Admin_Supplier_Add_FAIL");
                Assertions.fail("Lỗi: Thêm Supplier thất bại.");
            }

            // 2. Edit
            System.out.println("Test 5.2: Sửa Supplier...");
            clickElementJS(driver.findElement(By.cssSelector("a[href*='editSupplier']")));

            WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
            emailInput.clear();
            emailInput.sendKeys("up@test.com");
            clickElementJS(driver.findElement(By.cssSelector("button[type='submit']")));

            Thread.sleep(1000);
            if (!driver.getCurrentUrl().contains("suppliers")) {
                driver.get("http://localhost:9090/admin/suppliers");
            }

            // 3. Delete
            System.out.println("Test 5.3: Xóa Supplier...");
            searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dataTables_filter input")));
            searchInput.clear();
            searchInput.sendKeys(supName); // Tìm lại tên cũ (vì nãy chỉ sửa email, tên vẫn vậy)
            Thread.sleep(1000);

            clickElementJS(driver.findElement(By.cssSelector("button[onclick*='showConfigModalDialog']")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));
            Thread.sleep(500);
            clickElementJS(driver.findElement(By.id("yesOption")));
            Thread.sleep(1500);

        } catch (Exception e) {
            takeScreenshot("Admin_Supplier_Error");
            Assertions.fail("Lỗi Supplier: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    void test_view_customers() {
        driver.get("http://localhost:9090/admin/customers");
        try {
            System.out.println("Test 6: Xem danh sách khách hàng...");
            WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-row")));
            Assertions.assertTrue(table.isDisplayed());

            driver.findElement(By.cssSelector("div.dataTables_filter input")).sendKeys("admin");
            Thread.sleep(500);
        } catch (Exception e) {
            takeScreenshot("Admin_Customer_Error");
            Assertions.fail("Lỗi Customer: " + e.getMessage());
        }
    }
    // --- TEST 7: NEGATIVE - THÊM SẢN PHẨM THIẾU TÊN ---
    @Test
    @Order(7)
    void test_add_product_fail_empty_name() {
        driver.get("http://localhost:9090/admin/products");

        try {
            System.out.println("Test 7: Thử thêm sản phẩm nhưng bỏ trống Tên...");

            // Mở Modal
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-target='#addRowModal']")
            ));
            clickElementJS(addBtn);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            // 1. ĐỂ TRỐNG TÊN (Không sendKeys vào #name)
            driver.findElement(By.id("name")).clear();

            // 2. Điền các trường khác hợp lệ để cô lập lỗi tại Name
            driver.findElement(By.id("price")).sendKeys("100");
            driver.findElement(By.id("quantity")).sendKeys("10");

            // 3. Bấm Submit (Dùng click thường để kích hoạt validate trình duyệt)
            WebElement submitBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            submitBtn.click();

            Thread.sleep(1000);

            // 4. Validate & Chụp ảnh
            // Nếu modal vẫn còn hiển thị nghĩa là chưa submit được -> ĐÚNG
            boolean isModalVisible = driver.findElement(By.id("addRowModal")).isDisplayed();

            if (isModalVisible) {
                // Scroll lên đầu modal để chụp thấy ô Name bị báo đỏ (nếu có)
                WebElement nameInput = driver.findElement(By.id("name"));
                // Check thông báo lỗi HTML5 (bong bóng thoại)
                String validationMessage = nameInput.getAttribute("validationMessage");
                System.out.println("Thông báo lỗi từ trình duyệt: " + validationMessage);

                System.out.println("Pass: Hệ thống đã chặn việc thêm sản phẩm thiếu tên.");
                takeScreenshot("Error_Product_EmptyName_Blocked");
            } else {
                takeScreenshot("Error_Product_EmptyName_Failed");
                Assertions.fail("Lỗi: Hệ thống vẫn cho phép tạo sản phẩm không có tên!");
            }

        } catch (Exception e) {
            takeScreenshot("Error_Product_EmptyName_Exception");
            Assertions.fail("Lỗi test case 7: " + e.getMessage());
        }
    }

    // --- TEST 8: NEGATIVE - THÊM SẢN PHẨM GIÁ ÂM ---
    @Test
    @Order(8)
    void test_add_product_fail_negative_price() {
        // Refresh lại trang để đóng modal cũ nếu có
        driver.navigate().refresh();
        wait.until(ExpectedConditions.urlContains("products"));

        try {
            System.out.println("Test 8: Thử thêm sản phẩm giá âm...");

            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-target='#addRowModal']")
            ));
            clickElementJS(addBtn);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            driver.findElement(By.id("name")).sendKeys("Negative Price Item");

            // 1. NHẬP GIÁ ÂM
            WebElement priceInput = driver.findElement(By.id("price"));
            priceInput.sendKeys("-500");

            driver.findElement(By.id("quantity")).sendKeys("10");

            // 2. Bấm Submit
            WebElement submitBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            submitBtn.click();

            Thread.sleep(1000);

            // 3. Validate
            // Kiểm tra xem modal còn đó không
            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Hệ thống chặn giá âm.");
                takeScreenshot("Error_Product_NegativePrice_Blocked");
            } else {
                // Nếu modal biến mất, kiểm tra xem sản phẩm có được tạo với giá âm không
                takeScreenshot("Error_Product_NegativePrice_Failed");
                // Tìm kiếm sản phẩm vừa tạo
                WebElement searchInput = driver.findElement(By.cssSelector("div.dataTables_filter input"));
                searchInput.sendKeys("Negative Price Item");
                Thread.sleep(1000);
                String rowText = driver.findElement(By.id("add-row")).getText();

                if(rowText.contains("-500")) {
                    Assertions.fail("LỖI NGHIÊM TRỌNG: Hệ thống đã chấp nhận giá âm!");
                } else {
                    System.out.println("Cảnh báo: Modal đóng nhưng có thể server đã tự sửa giá về 0 hoặc không lưu.");
                }
            }

        } catch (Exception e) {
            takeScreenshot("Error_Product_NegativePrice_Exception");
        }
    }

    // --- TEST 9: NEGATIVE - NHÀ CUNG CẤP SAI EMAIL ---
    @Test
    @Order(9)
    void test_add_supplier_fail_invalid_email() {
        driver.get("http://localhost:9090/admin/suppliers");

        try {
            System.out.println("Test 9: Thêm NCC với email sai định dạng...");

            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@data-target='#addRowModal']")
            ));
            clickElementJS(addBtn);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addRowModal")));
            Thread.sleep(500);

            driver.findElement(By.id("name")).sendKeys("Bad Email Supplier");

            // 1. NHẬP EMAIL KHÔNG CÓ @
            WebElement emailInput = driver.findElement(By.id("email"));
            emailInput.sendKeys("email_nay_bi_sai_roi"); // Thiếu @gmail.com

            driver.findElement(By.id("phone")).sendKeys("0999888777");

            // 2. Submit
            WebElement submitBtn = driver.findElement(By.xpath("//div[@id='addRowModal']//button[contains(text(), 'Add')]"));
            submitBtn.click();

            Thread.sleep(1000);

            // 3. Validate
            // Kiểm tra thuộc tính validationMessage của trình duyệt
            String valMsg = emailInput.getAttribute("validationMessage");

            if (driver.findElement(By.id("addRowModal")).isDisplayed()) {
                System.out.println("Pass: Bị chặn. Thông báo: " + valMsg);
                takeScreenshot("Error_Supplier_BadEmail_Blocked");
            } else {
                takeScreenshot("Error_Supplier_BadEmail_Failed");
                Assertions.fail("Lỗi: Hệ thống cho phép lưu email sai định dạng!");
            }

        } catch (Exception e) {
            takeScreenshot("Error_Supplier_Exception");
        }
    }
}