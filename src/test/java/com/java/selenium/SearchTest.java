package com.java.selenium;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayInputStream;
import java.io.File;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ExtendWith(ScreenshotOnFailureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SearchTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 10;

    @BeforeEach
    void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    // --- HÀM CLICK JS ---
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

    // --- TEST 1: TÌM KIẾM KEYWORD (SỬA LOCATOR) ---
    @Test
    @Order(1)
    void test_search_by_keyword_success() {
        driver.get("http://localhost:9090/");

        try {
            System.out.println("Test 1: Tìm kiếm 'Snack'...");

            // Tìm ô input
            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@placeholder, 'Search')]")
            ));

            searchInput.clear();
            searchInput.sendKeys("Snack");

            // SỬA LOCATOR NÚT SEARCH: Tìm nút button nằm ngay sau thẻ input
            WebElement searchBtn = driver.findElement(By.xpath("//input[contains(@placeholder, 'Search')]/following-sibling::button"));

            clickElementJS(searchBtn);

            // Validate
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-card")));
            List<WebElement> productNames = driver.findElements(By.cssSelector(".product-name h6 a"));

            Assertions.assertTrue(productNames.size() > 0, "Không tìm thấy sản phẩm nào!");

            String firstProductName = productNames.get(0).getText().toLowerCase();
            Assertions.assertTrue(firstProductName.contains("snack"), "Tên sản phẩm không đúng: " + firstProductName);

            takeScreenshot("Search_Keyword_PASS");

        } catch (Exception e) {
            takeScreenshot("Search_Keyword_Error");
            Assertions.fail("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // --- TEST 2: TÌM KIẾM KHÔNG CÓ KẾT QUẢ ---
    @Test
    @Order(2)
    void test_search_no_result() {
        driver.get("http://localhost:9090/");

        try {
            System.out.println("Test 2: Tìm kiếm sai...");

            WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[contains(@placeholder, 'Search')]")
            ));

            searchInput.clear();
            searchInput.sendKeys("Iphone 16 Pro Max");

            WebElement searchBtn = driver.findElement(By.xpath("//input[contains(@placeholder, 'Search')]/following-sibling::button"));
            clickElementJS(searchBtn);

            Thread.sleep(1500);

            List<WebElement> products = driver.findElements(By.cssSelector(".product-card"));

            if (products.isEmpty()) {
                // Chụp ảnh bằng chứng list rỗng (Hàm takeScreenshot sẽ tự cuộn lên đầu)
                takeScreenshot("Search_NoResult_PASS");
                Assertions.assertTrue(true);
            } else {
                takeScreenshot("Search_NoResult_FAIL");
                Assertions.fail("Lỗi: Vẫn tìm thấy sản phẩm!");
            }

        } catch (Exception e) {
            takeScreenshot("Search_NoResult_Error");
            Assertions.fail("Lỗi test: " + e.getMessage());
        }
    }

    // --- TEST 3: TÌM THEO DANH MỤC ---
    @Test
    @Order(3)
    void test_filter_by_category() {
        driver.get("http://localhost:9090/");

        try {
            // Tìm Menu Categories (dùng dấu chấm để tìm text chứa trong thẻ con)
            WebElement categoryMenu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(., 'Categories')]")
            ));

            Actions actions = new Actions(driver);
            actions.moveToElement(categoryMenu).perform();
            Thread.sleep(500);

            // Tìm list danh mục con
            List<WebElement> subCategories = driver.findElements(By.xpath("//a[contains(., 'Categories')]/following-sibling::ul//a"));

            if (subCategories.isEmpty()) {
                clickElementJS(categoryMenu);
            } else {
                clickElementJS(subCategories.get(0));
            }

            wait.until(ExpectedConditions.urlContains("product"));

            // Chụp ảnh kết quả lọc danh mục
            takeScreenshot("Search_Category_PASS");

            List<WebElement> products = driver.findElements(By.cssSelector(".product-card"));
            Assertions.assertTrue(products.size() > 0, "Danh mục rỗng!");

        } catch (Exception e) {
            takeScreenshot("Search_Category_Error");
            Assertions.fail("Lỗi danh mục: " + e.getMessage());
        }
    }
}