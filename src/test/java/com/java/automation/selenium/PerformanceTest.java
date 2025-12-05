package com.java.automation.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

@Listeners(TestListener.class)
public class PerformanceTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final long MAX_LOAD_TIME_MS = 5000;
    private static final int TIMEOUT = 15;

    @BeforeMethod
    void setUp() {
        // QUAN TRỌNG: Không khởi tạo driver mới ở đây nữa!
        // Chúng ta dùng driver static từ BaseSeleniumTest để chỉ mở 1 trình duyệt duy nhất.
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
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
        driver.get(BASE_URL + "login");
        try {
            if (!driver.getCurrentUrl().contains("login")) return;

            driver.findElement(By.name("customerId")).sendKeys("abcd");
            driver.findElement(By.name("password")).sendKeys("123123");

            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in')]"));
            clickElementJS(loginBtn);

            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        } catch (Exception e) {
            System.out.println("Info Login: " + e.getMessage());
        }
    }

    private void ensureCartHasProduct() {
        driver.get(BASE_URL + "carts");
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table.table-list tbody tr"));
            if (rows.isEmpty()) {
                System.out.println("🛒 Giỏ hàng rỗng -> Đang đi thêm hàng...");
                driver.get(BASE_URL + "products");
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-btn a")));
                clickElementJS(driver.findElements(By.cssSelector(".product-btn a")).get(0));

                Thread.sleep(1500);
                driver.get(BASE_URL + "carts");
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
            Object val = js.executeScript("return window.performance.timing.loadEventEnd;");
            if (val instanceof Number && ((Number) val).longValue() > 0) {
                loadEventEnd = ((Number) val).longValue();
                break;
            }
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }

        if (loadEventEnd == 0) {
            Object val = js.executeScript("return window.performance.timing.responseEnd;");
            if (val instanceof Number) loadEventEnd = ((Number) val).longValue();
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

        System.out.println("==================================================");
        System.out.println("📊 REPORT: " + pageName);
        System.out.println("   🔗 URL: " + driver.getCurrentUrl());
        System.out.println("   ⏱️ Total Load Time: " + loadTime + " ms");
        System.out.println("   📡 Server Latency: " + latency + " ms");
        System.out.println("   🎨 DOM Render Time: " + renderTime + " ms");
        System.out.println("==================================================");

        if (loadTime > MAX_LOAD_TIME_MS) {
            System.err.println("⚠️ CẢNH BÁO: Trang " + pageName + " tải chậm (" + loadTime + "ms)");
        } else {
            System.out.println("✅ Hiệu năng tốt.");
        }
    }

    // --- CÁC TEST CASE ---

    @Test(priority = 1)
    void test_home_page_performance() {
        driver.get(BASE_URL);
        measurePerformance("Home Page");
    }

    @Test(priority = 2)
    void test_product_page_performance() {
        driver.get(BASE_URL + "products");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-card")));
        measurePerformance("Product List Page");
    }

    @Test(priority = 3)
    void test_product_detail_performance() {
        driver.get(BASE_URL + "products");

        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".product-name a")
        ));

        clickElementJS(productLink);

        wait.until(ExpectedConditions.urlContains("productDetail"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));

        measurePerformance("Product Detail Page");
    }

    @Test(priority = 4)
    void test_admin_flow_performance() {
        driver.get(BASE_URL + "login");

        // Login Admin
        if (driver.findElements(By.name("customerId")).size() > 0) {
            driver.findElement(By.name("customerId")).sendKeys("admin");
            driver.findElement(By.name("password")).sendKeys("123123");
            WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(), 'sign in')]"));
            clickElementJS(loginBtn);
        }

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe(BASE_URL),
                    ExpectedConditions.urlContains("/admin")
            ));
        } catch (Exception e) {}

        if (!driver.getCurrentUrl().contains("admin/home")) {
            driver.get(BASE_URL + "admin/home");
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Dashboard')]")));
        measurePerformance("Load Admin Dashboard");

        System.out.println("👉 Measuring: Navigate to Product Management...");
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

        System.out.println("👉 Measuring: Navigate to Order Management...");
        WebElement orderMenu = driver.findElement(By.xpath("//span[contains(text(), 'Order Management')] | //a[contains(text(), 'Order Management')]"));
        clickElementJS(orderMenu);

        wait.until(ExpectedConditions.urlContains("orders"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-row")));
        measurePerformance("Navigate to Order Management");
    }

    @Test(priority = 5)
    void test_add_to_cart_performance() {
        ensureLoggedIn();
        driver.get(BASE_URL + "products");

        System.out.println("👉 Measuring: Click Add -> Load Cart");

        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".product-btn a")
        ));

        clickElementJS(addBtn);

        // Chờ server xử lý ngầm
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        // Chủ động vào trang cart để đo
        driver.get(BASE_URL + "carts");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

        measurePerformance("Action: Add To Cart (Load Cart)");
    }

    @Test(priority = 6)
    void test_checkout_page_performance() {
        ensureLoggedIn();
        ensureCartHasProduct();

        if (!driver.getCurrentUrl().contains("cart")) {
            driver.get(BASE_URL + "carts");
        }

        System.out.println("👉 Measuring: Click Checkout -> Load Checkout Page");

        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href, 'checkout')] | //button[contains(text(), 'Check Out') or contains(text(), 'Thanh toán')]")
        ));

        clickElementJS(checkoutBtn);

        wait.until(ExpectedConditions.urlContains("checkout"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("receiver")));

        measurePerformance("Action: Go to Checkout Page");
    }
}