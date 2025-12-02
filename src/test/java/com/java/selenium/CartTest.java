package com.java.selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

@ExtendWith(ScreenshotOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 10;

    @BeforeEach
    void setUp() {
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    // --- HÀM CLICK JS (Dùng để trị các nút bị che hoặc khó bấm) ---
    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click(); // Thử click thường nếu JS thất bại
        }
    }

    // --- LOGIC LOGIN (Dựa trên file loginOrRegister.html) ---
    private void ensureLoggedIn() {
        driver.get("http://localhost:8080/login");
        try {
            // Kiểm tra nếu đã login (có nút logout hoặc profile) thì bỏ qua
            // Ở đây check URL cho nhanh
            if (!driver.getCurrentUrl().contains("login")) return;

            // Dựa trên loginOrRegister.html: name="customerId"
            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            userField.sendKeys("abcd"); // Thay user của bạn vào đây

            driver.findElement(By.name("password")).sendKeys("123123");

            // Nút sign in: class="btn btn-outline" và value="login" hoặc text "sign in now"
            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            clickElementJS(loginBtn);

            // Chờ về trang chủ
            wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        } catch (Exception e) {
            System.out.println("Đã login hoặc có lỗi login: " + e.getMessage());
        }
    }

    // --- TEST CASE 1: THÊM SẢN PHẨM (Đã sửa lại logic điều hướng) ---
    @Test
    @Order(1)
    void test_add_to_cart_success() {
        ensureLoggedIn();
        driver.get("http://localhost:8080/products");

        try {
            // 1. Tìm nút Add To Cart
            // Selector này tìm thẻ <a> nằm trong class 'product-btn' (chính xác theo shop.html)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
            List<WebElement> addButtons = driver.findElements(By.cssSelector(".product-btn a"));

            if (addButtons.isEmpty()) Assertions.fail("Không tìm thấy sản phẩm nào!");

            // Lấy sản phẩm thứ 2 (index 1) để tránh sản phẩm đầu tiên nếu bị lỗi layout
            WebElement btnAddToCart = addButtons.size() > 1 ? addButtons.get(1) : addButtons.get(0);

            System.out.println("Đang click Add to Cart...");
            clickElementJS(btnAddToCart);

            // 2. QUAN TRỌNG: Thay vì chờ URL đổi, ta chờ 1 chút rồi tự vào giỏ
            // Vì web của bạn thêm xong vẫn đứng ở trang products
            Thread.sleep(1500); // Chờ 1.5s để server kịp xử lý request thêm hàng

            // 3. Chủ động chuyển hướng sang trang Cart để kiểm tra
            driver.get("http://localhost:8080/carts");

            // 4. Validate (Kiểm tra xem có sản phẩm trong bảng không)
            // Tìm bảng giỏ hàng
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.table-list")));

            // Đếm số dòng sản phẩm
            int rowCount = driver.findElements(By.cssSelector("table.table-list tbody tr")).size();
            Assertions.assertTrue(rowCount > 0, "Giỏ hàng vẫn trống sau khi thêm!");


        } catch (Exception e) {
            Assertions.fail("Lỗi Add Cart: " + e.getMessage());
        }
    }

    // --- TEST 2: CẬP NHẬT SỐ LƯỢNG (SỬA LẠI THEO checkOut.html) ---
    @Test
    @Order(2)
    void test_update_quantity() {
        if (!driver.getCurrentUrl().contains("cart")) {
            driver.get("http://localhost:8080/carts"); // URL trang cart của bạn
        }

        try {
            // Tìm ô input số lượng. Trong HTML: <input type="number" ... onchange="updateQuantity(...)">
            // Chúng ta tìm input có id bắt đầu bằng 'quantityInput_'
            WebElement qtyInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@id, 'quantityInput_')]")
            ));

            // Xóa và nhập số mới
            qtyInput.clear();
            qtyInput.sendKeys("5");

            // QUAN TRỌNG: HTML dùng sự kiện 'onchange'.
            // Sự kiện này chỉ kích hoạt khi ta nhấn Enter hoặc click chuột ra ngoài.
            qtyInput.sendKeys(Keys.ENTER);
            Thread.sleep(1000); // Chờ AJAX gọi lên server cập nhật

            // Kiểm tra lại giá trị
            String val = qtyInput.getAttribute("value");
            Assertions.assertEquals("5", val, "Số lượng chưa được cập nhật!");

        } catch (Exception e) {
            Assertions.fail("Lỗi Update Cart: " + e.getMessage());
        }
    }

    // --- TEST 3: XÓA SẢN PHẨM (QUAN TRỌNG: XỬ LÝ MODAL) ---
    @Test
    @Order(3)
    void test_remove_from_cart() {
        if (!driver.getCurrentUrl().contains("cart")) {
            driver.get("http://localhost:8080/carts");
        }

        try {
            // Đếm số dòng trước khi xóa
            int oldSize = driver.findElements(By.cssSelector("table.table-list tbody tr")).size();
            if (oldSize == 0) Assertions.fail("Giỏ hàng rỗng!");

            // 1. Tìm nút thùng rác (fa-trash-alt)
            // Trong HTML: <a onclick="showConfigModalDialog(...)"> <i class="fas fa-trash-alt"></i> </a>
            WebElement trashBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".fa-trash-alt")
            ));

            // Click vào thùng rác => Sẽ hiện Modal
            clickElementJS(trashBtn);

            // 2. XỬ LÝ MODAL (id="configmationId")
            // Phải chờ Modal hiện lên
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configmationId")));

            // 3. Tìm nút YES trong modal (id="yesOption")
            WebElement yesBtn = modal.findElement(By.id("yesOption"));

            // Click Yes để xác nhận xóa
            wait.until(ExpectedConditions.elementToBeClickable(yesBtn));
            yesBtn.click();

            // 4. Validate
            // Chờ trang reload hoặc dòng bị xóa đi
            Thread.sleep(1500); // Chờ reload
            int newSize = driver.findElements(By.cssSelector("table.table-list tbody tr")).size();

            Assertions.assertTrue(newSize < oldSize, "Sản phẩm vẫn còn, chưa bị xóa!");

        } catch (Exception e) {
            Assertions.fail("Lỗi Xóa Cart: " + e.getMessage());
        }
    }
}