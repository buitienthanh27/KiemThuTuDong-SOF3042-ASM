package com.java.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.openqa.selenium.remote.ErrorCodes.TIMEOUT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final long MAX_LOAD_TIME_MS = 5000;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- CÁC HÀM HỖ TRỢ ---

    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    private void ensureLoggedIn() {
        driver.get("http://localhost:8080/login");
        try {
            if (!driver.getCurrentUrl().contains("login")) return;

            driver.findElement(By.name("customerId")).sendKeys("abcd"); // User thường
            driver.findElement(By.name("password")).sendKeys("123123");

            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in')]"));
            clickElementJS(loginBtn);

            wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        } catch (Exception e) {
            System.out.println("Info Login: " + e.getMessage());
        }
    }

    private void ensureCartHasProduct() {
        driver.get("http://localhost:8080/carts");
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.table-list tbody tr"));
            if (rows.isEmpty()) {
                System.out.println("🛒 Giỏ hàng rỗng -> Đang đi thêm hàng...");
                driver.get("http://localhost:8080/products");
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
                clickElementJS(driver.findElements(By.cssSelector(".product-btn a")).get(0));
                wait.until(ExpectedConditions.urlContains("cart"));
            }
        } catch (Exception e) {
            System.out.println("Lỗi check giỏ hàng: " + e.getMessage());
        }
    }

    private void measurePerformance(String pageName) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));

        long loadEventEnd = 0;
        for(int i=0; i<20; i++) {
            loadEventEnd = (Long) js.executeScript("return window.performance.timing.loadEventEnd;");
            if(loadEventEnd > 0) break;
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }

        if (loadEventEnd == 0) {
            loadEventEnd = (Long) js.executeScript("return window.performance.timing.responseEnd;");
        }

        Long loadTime = (Long) js.executeScript(
                "return arguments[0] - performance.timing.navigationStart;", loadEventEnd
        );
        Long latency = (Long) js.executeScript(
                "return performance.timing.responseStart - performance.timing.requestStart;"
        );
        Long renderTime = (Long) js.executeScript(
                "return performance.timing.domComplete - performance.timing.domLoading;"
        );

        System.out.println("--------------------------------------------------");
        System.out.println("📊 REPORT: " + pageName);
        System.out.println("   🔗 URL: " + driver.getCurrentUrl());
        System.out.println("   ⏱️ Total Load Time: " + loadTime + " ms");
        System.out.println("   📡 Server Latency: " + latency + " ms");
        System.out.println("   🎨 DOM Render Time: " + renderTime + " ms");
        System.out.println("--------------------------------------------------");

        if (loadTime > MAX_LOAD_TIME_MS) {
            System.err.println("⚠️ CẢNH BÁO: Trang " + pageName + " tải chậm (" + loadTime + "ms)");
        } else {
            System.out.println("✅ Hiệu năng tốt.");
        }
    }

    // --- CÁC TEST CASE ---

    @Test
    @Order(1)
    void test_home_page_performance() {
        driver.get("http://localhost:8080/");
        measurePerformance("Home Page");
    }

    @Test
    @Order(2)
    void test_product_page_performance() {
        driver.get("http://localhost:8080/products");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-card")));
        measurePerformance("Product List Page");
    }

    // Test 3: Đã sửa đường dẫn bắt đầu từ /products
    @Test
    @Order(3)
    void test_product_detail_performance() {
        driver.get("http://localhost:8080/products");

        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".product-name a")
        ));

        clickElementJS(productLink);

        wait.until(ExpectedConditions.urlContains("productDetail"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));

        measurePerformance("Product Detail Page");
    }

    // Test 4: Đã sửa đường dẫn Admin thành /admin/home
    @Test
    @Order(4)
    void test_admin_flow_performance() {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.name("customerId")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("123123");

        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in')]"));
        clickElementJS(loginBtn);

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe("http://localhost:8080/"),
                    ExpectedConditions.urlContains("/admin")
            ));
        } catch (Exception e) {
            System.out.println("Lưu ý: Login xong URL lạ: " + driver.getCurrentUrl());
        }

        // SỬA: Trỏ đúng về /admin/home
        if (!driver.getCurrentUrl().contains("admin/home")) {
            driver.get("http://localhost:8080/admin/home");
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Dashboard')]")));
        measurePerformance("Load Admin Dashboard");

        System.out.println("👉 Đang click menu 'Product Management'...");
        try {
            List<WebElement> parentMenus = driver.findElements(By.xpath("//p[contains(text(), 'Management System')]"));
            if (!parentMenus.isEmpty()) {
                clickElementJS(parentMenus.get(0));
                Thread.sleep(500);
            }
        } catch (Exception ignored) {}

        WebElement productMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(text(), 'Product Management')] | //a[contains(text(), 'Product Management')]")
        ));
        clickElementJS(productMenu);

        wait.until(ExpectedConditions.urlContains("products"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-row")));

        measurePerformance("Navigate to Product Management");

        System.out.println("👉 Đang click menu 'Order Management'...");
        WebElement orderMenu = driver.findElement(By.xpath("//span[contains(text(), 'Order Management')] | //a[contains(text(), 'Order Management')]"));
        clickElementJS(orderMenu);

        wait.until(ExpectedConditions.urlContains("orders"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-row")));

        measurePerformance("Navigate to Order Management");
    }

    // --- TEST 5: ĐO HIỆU NĂNG THÊM GIỎ HÀNG (ĐÃ SỬA LỖI TIMEOUT) ---
    @Test
    @Order(5)
    void test_add_to_cart_performance() {
        ensureLoggedIn(); // Phải login trước
        driver.get("http://localhost:8080/products");

        System.out.println("👉 Đang đo: Click Add to Cart -> Load Cart");

        // 1. Tìm nút Add to Cart
        // Dùng selector .product-btn a để bắt đúng nút thêm (tránh nhầm nút xem chi tiết)
        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".product-btn a")
        ));

        // 2. Click Thêm (Web sẽ xử lý ngầm, không chuyển trang ngay)
        clickElementJS(addBtn);

        // 3. Chờ 1.5s để đảm bảo Server đã thêm hàng vào giỏ
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        // 4. CHỦ ĐỘNG CHUYỂN HƯỚNG SANG GIỎ HÀNG
        // (Vì web không tự chuyển, ta phải bấm vào icon giỏ hàng hoặc đi thẳng link)
        driver.get("http://localhost:8080/carts");

        // 5. Chờ trang Cart load xong nội dung (Bảng sản phẩm)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

        // 6. Đo hiệu năng tải trang Cart
        measurePerformance("Action: Load Cart Page (After Add)");
    }

    // --- TEST 6: ĐO HIỆU NĂNG TRANG THANH TOÁN (Mới) ---
    @Test
    @Order(6)
    void test_checkout_page_performance() {
        ensureLoggedIn();
        ensureCartHasProduct();

        if (!driver.getCurrentUrl().contains("cart")) {
            driver.get("http://localhost:8080/carts");
        }

        System.out.println("👉 Đang đo: Click Checkout -> Load Checkout Page");

        // Tìm nút Checkout (dựa trên HTML cũ của bạn là thẻ a href checkout)
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href, 'checkout')] | //button[contains(text(), 'Check Out') or contains(text(), 'Thanh toán')]")
        ));

        clickElementJS(checkoutBtn);

        wait.until(ExpectedConditions.urlContains("checkout"));
        // Chờ form điền tên hiện ra
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("receiver")));

        measurePerformance("Action: Go to Checkout Page");
    }
}