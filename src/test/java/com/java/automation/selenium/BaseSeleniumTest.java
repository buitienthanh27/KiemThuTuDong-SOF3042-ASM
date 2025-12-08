package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public class BaseSeleniumTest {
    protected static WebDriver driver;
    protected String BASE_URL = "http://localhost:9090/";

    @BeforeMethod
    public void baseSetUp() {
        // 1. Setup Driver
        WebDriverManager.chromedriver().setup();

        // 2. Cấu hình Chrome Options cho GitHub Actions/Linux
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        // --- BẮT BUỘC PHẢI CÓ CÁC DÒNG NÀY CHO CI/CD ---
        options.addArguments("--headless=new"); // Chạy không giao diện
        options.addArguments("--no-sandbox");   // Bắt buộc cho quyền root trong Docker/Linux
        options.addArguments("--disable-dev-shm-usage"); // Fix lỗi crash memory
        options.addArguments("--window-size=1920,1080"); // Giả lập màn hình Full HD
        options.addArguments("--disable-gpu");
        // -----------------------------------------------

        driver = new ChromeDriver(options);

        // 3. Lấy URL từ TestConfig (Đúng chuẩn)
        try {
            String configUrl = TestConfig.getBaseUrl();
            if (configUrl != null && !configUrl.isEmpty()) {
                BASE_URL = configUrl;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Không đọc được URL từ config, dùng mặc định.");
        }

        // Chuẩn hóa URL
        if (!BASE_URL.endsWith("/")) {
            BASE_URL += "/";
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        // Không dùng maximize() ở chế độ headless, đã set window-size ở trên
    }

    @AfterMethod
    public void baseTearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Hàm hỗ trợ click an toàn
    protected void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(200);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            try {
                element.click();
            } catch (Exception ex) {
                System.out.println("Click failed: " + ex.getMessage());
            }
        }
    }
}