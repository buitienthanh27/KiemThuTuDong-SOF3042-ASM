package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
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
        // Cấu hình chuẩn cho GitHub Actions
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");

        driver = new ChromeDriver(options);

        try {
            String configUrl = TestConfig.getBaseUrl();
            if (configUrl != null && !configUrl.isEmpty()) {
                BASE_URL = configUrl;
            }
        } catch (Exception e) {}

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

    // --- HÀM CLICK THÔNG MINH (QUAN TRỌNG) ---
    protected void smartClick(WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // Cách 1: Chờ click được và click thường
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            try {
                // Cách 2: JS Click
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                Thread.sleep(200);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception ex) {
                // Cách 3: Actions Click (Fallback cuối cùng)
                System.out.println("⚠️ SmartClick failed, trying Actions: " + ex.getMessage());
            }
        }
    }

    // Hàm chờ trang load xong hoàn toàn (JS ready)
    protected void waitForPageLoaded() {
        ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
            }
        };
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(expectation);
        } catch (Throwable error) {
            System.out.println("⚠️ Timeout waiting for Page Load Request to complete.");
        }
    }

    // Giữ hàm cũ để tương thích code cũ
    protected void clickElementJS(WebElement element) {
        smartClick(element);
    }

    protected void takeScreenshot(String fileName) {
        if (driver == null) return;
        try {
            Path dirPath = Paths.get("test-output", "screenshots");
            if (!Files.exists(dirPath)) Files.createDirectories(dirPath);

            File srcFile = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            String timestamp = String.valueOf(System.currentTimeMillis());
            Files.copy(srcFile.toPath(), dirPath.resolve(fileName + "_" + timestamp + ".png"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("Screenshot Error: " + e.getMessage());
        }
    }
}