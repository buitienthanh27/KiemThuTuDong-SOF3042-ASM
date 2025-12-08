package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class BaseSeleniumTest {
    protected static WebDriver driver;
    protected String BASE_URL = "http://localhost:9090/";

    @BeforeMethod
    public void baseSetUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        // --- CẤU HÌNH QUAN TRỌNG CHO GITHUB ACTIONS ---
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");

        driver = new ChromeDriver(options);

        // Lấy URL từ Config
        try {
            String configUrl = TestConfig.getBaseUrl();
            if (configUrl != null && !configUrl.isEmpty()) {
                BASE_URL = configUrl;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Không đọc được URL từ config, dùng mặc định.");
        }

        if (!BASE_URL.endsWith("/")) {
            BASE_URL += "/";
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterMethod
    public void baseTearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

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

    // --- FIX LỖI NULL POINTER KHI CHỤP ẢNH ---
    protected void takeScreenshot(String fileName) {
        if (driver == null) {
            System.out.println("⚠️ Driver is null, cannot take screenshot: " + fileName);
            return;
        }

        try {
            // Tạo thư mục nếu chưa có
            Path dirPath = Paths.get("test-output", "screenshots");
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = String.valueOf(System.currentTimeMillis());
            Path destPath = dirPath.resolve(fileName + "_" + timestamp + ".png");

            Files.copy(srcFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("📸 Saved screenshot: " + destPath.toString());

        } catch (Exception e) {
            System.out.println("❌ Failed to save screenshot: " + e.getMessage());
        }
    }
}