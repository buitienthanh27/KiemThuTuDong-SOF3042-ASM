package com.java.automation.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

import static com.java.automation.utils.ScreenshotUtil.takeScreenshot;

@Listeners(TestListener.class)
public class SearchTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 10;

    // Biến lưu URL chuẩn hóa
    private String homeUrlWithSlash;

    @BeforeMethod
    void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));

        // Chuẩn bị URL an toàn (tránh lỗi timeout do thiếu dấu /)
        String rawBase = BASE_URL;
        if (rawBase == null) rawBase = "http://localhost:9090/";
        String homeUrlNoSlash = rawBase.endsWith("/") ? rawBase.substring(0, rawBase.length() - 1) : rawBase;
        homeUrlWithSlash = homeUrlNoSlash + "/";
    }

    // --- HÀM CLICK JS (Tối ưu) ---
    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            // Wait cực ngắn để UI ổn định (thay vì sleep cứng 500ms)
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    // --- TEST 1: TÌM KIẾM KEYWORD ---
    @Test(priority = 1)
    void test_search_by_keyword_success() {
        driver.get(homeUrlWithSlash);

        try {
            System.out.println("Test 1: Tìm kiếm 'Snack'...");

            // Tìm ô input
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@placeholder, 'Search')]")
            ));

            searchInput.clear();
            searchInput.sendKeys("Snack");

            // Tìm nút button nằm ngay sau thẻ input
            WebElement searchBtn = driver.findElement(By.xpath("//input[contains(@placeholder, 'Search')]/following-sibling::button"));

            clickElementJS(searchBtn);

            // Validate: Chờ ít nhất 1 sản phẩm xuất hiện
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-card")));
            List<WebElement> productNames = driver.findElements(By.cssSelector(".product-name h6 a"));

            Assert.assertTrue(productNames.size() > 0, "Không tìm thấy sản phẩm nào!");

            String firstProductName = productNames.get(0).getText().toLowerCase();
            Assert.assertTrue(firstProductName.contains("snack"), "Tên sản phẩm đầu tiên không chứa từ khóa: " + firstProductName);

        } catch (Exception e) {
            takeScreenshot("Search_Keyword_Error");
            Assert.fail("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // --- TEST 2: TÌM KIẾM KHÔNG CÓ KẾT QUẢ ---
    @Test(priority = 2)
    void test_search_no_result() {
        driver.get(homeUrlWithSlash);

        try {
            System.out.println("Test 2: Tìm kiếm sai...");

            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@placeholder, 'Search')]")
            ));

            searchInput.clear();
            searchInput.sendKeys("Iphone 16 Pro Max");

            WebElement searchBtn = driver.findElement(By.xpath("//input[contains(@placeholder, 'Search')]/following-sibling::button"));
            clickElementJS(searchBtn);

            // Với test case check "Không có kết quả", ta bắt buộc phải chờ 1 chút để server phản hồi
            // Tuy nhiên, ta dùng wait thông minh: Chờ list sản phẩm biến mất (nếu đang hiển thị cái cũ)
            // Hoặc chờ 1 thông báo "No result" nếu có. Ở đây ta giữ sleep ngắn để chắc chắn request đã xong.
            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            List<WebElement> products = driver.findElements(By.cssSelector(".product-card"));

            if (products.isEmpty()) {
                takeScreenshot("Search_NoResult_PASS");
                Assert.assertTrue(true);
            } else {
                takeScreenshot("Search_NoResult_FAIL");
                Assert.fail("Lỗi: Vẫn tìm thấy " + products.size() + " sản phẩm!");
            }

        } catch (Exception e) {
            takeScreenshot("Search_NoResult_Error");
            Assert.fail("Lỗi test: " + e.getMessage());
        }
    }

    // --- TEST 3: TÌM THEO DANH MỤC ---
    @Test(priority = 3)
    void test_filter_by_category() {
        driver.get(homeUrlWithSlash);

        try {
            // Tìm Menu Categories
            WebElement categoryMenu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(., 'Categories')]")
            ));

            Actions actions = new Actions(driver);
            actions.moveToElement(categoryMenu).perform();

            // Wait ngắn để menu sổ xuống
            try { Thread.sleep(500); } catch (InterruptedException e) {}

            // Tìm list danh mục con
            List<WebElement> subCategories = driver.findElements(By.xpath("//a[contains(., 'Categories')]/following-sibling::ul//a"));

            if (subCategories.isEmpty()) {
                // Fallback nếu không có sub-menu thì click luôn menu cha
                clickElementJS(categoryMenu);
            } else {
                // Click danh mục con đầu tiên
                wait.until(ExpectedConditions.elementToBeClickable(subCategories.get(0)));
                clickElementJS(subCategories.get(0));
            }

            wait.until(ExpectedConditions.urlContains("product"));

            // Check xem có sản phẩm nào load ra không
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-card")));
            List<WebElement> products = driver.findElements(By.cssSelector(".product-card"));
            Assert.assertTrue(products.size() > 0, "Danh mục rỗng, không có sản phẩm nào!");

        } catch (Exception e) {
            takeScreenshot("Search_Category_Error");
            Assert.fail("Lỗi danh mục: " + e.getMessage());
        }
    }
}