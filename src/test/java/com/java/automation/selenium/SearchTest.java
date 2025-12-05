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

@Listeners(TestListener.class)
public class SearchTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 10;

    @BeforeMethod
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

    // --- TEST 1: TÌM KIẾM KEYWORD ---
    @Test(priority = 1)
    void test_search_by_keyword_success() {
        driver.get(BASE_URL);

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

            // Validate
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-card")));
            List<WebElement> productNames = driver.findElements(By.cssSelector(".product-name h6 a"));

            Assert.assertTrue(productNames.size() > 0, "Không tìm thấy sản phẩm nào!");

            String firstProductName = productNames.get(0).getText().toLowerCase();
            Assert.assertTrue(firstProductName.contains("snack"), "Tên sản phẩm không đúng: " + firstProductName);

            // takeScreenshot("Search_Keyword_PASS"); // Mở comment nếu muốn chụp khi Pass

        } catch (Exception e) {
            takeScreenshot("Search_Keyword_Error");
            Assert.fail("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    // --- TEST 2: TÌM KIẾM KHÔNG CÓ KẾT QUẢ ---
    @Test(priority = 2)
    void test_search_no_result() {
        driver.get(BASE_URL);

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
                // Chụp ảnh bằng chứng list rỗng
                takeScreenshot("Search_NoResult_PASS");
                Assert.assertTrue(true);
            } else {
                takeScreenshot("Search_NoResult_FAIL");
                Assert.fail("Lỗi: Vẫn tìm thấy sản phẩm!");
            }

        } catch (Exception e) {
            takeScreenshot("Search_NoResult_Error");
            Assert.fail("Lỗi test: " + e.getMessage());
        }
    }

    // --- TEST 3: TÌM THEO DANH MỤC ---
    @Test(priority = 3)
    void test_filter_by_category() {
        driver.get(BASE_URL);

        try {
            // Tìm Menu Categories
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
            // takeScreenshot("Search_Category_PASS"); // Mở comment nếu cần

            List<WebElement> products = driver.findElements(By.cssSelector(".product-card"));
            Assert.assertTrue(products.size() > 0, "Danh mục rỗng!");

        } catch (Exception e) {
            takeScreenshot("Search_Category_Error");
            Assert.fail("Lỗi danh mục: " + e.getMessage());
        }
    }
}