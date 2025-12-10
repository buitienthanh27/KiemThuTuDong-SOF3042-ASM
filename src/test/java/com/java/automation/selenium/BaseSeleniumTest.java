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
    // Để static theo code của bạn (lưu ý không chạy parallel được)
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

    // --- HÀM CLICK THÔNG MINH ---
    protected void smartClick(WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                Thread.sleep(200);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception ex) {
                System.out.println("⚠️ SmartClick failed: " + ex.getMessage());
            }
        }
    }

    protected void waitForPageLoaded() {
        ExpectedCondition<Boolean> expectation = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(expectation);
        } catch (Throwable error) {
            System.out.println("⚠️ Timeout waiting for Page Load.");
        }
    }

    protected void clickElementJS(WebElement element) {
        smartClick(element);
    }

    public String takeScreenshot(String fileName) {
        if (driver == null) {
            System.out.println("⚠️ Driver is null, cannot take screenshot.");
            return null;
        }
        try {
            // SỬA: Thay đổi đường dẫn để lưu vào thư mục 'screenshots' ở thư mục gốc dự án
            String projectPath = System.getProperty("user.dir");
            Path dirPath = Paths.get(projectPath, "screenshots");

            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = String.valueOf(System.currentTimeMillis());

            // Đặt tên file an toàn hơn (tránh ký tự đặc biệt)
            String cleanFileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String fullFileName = "FAIL_" + cleanFileName + "_" + timestamp + ".png";

            Path destPath = dirPath.resolve(fullFileName);

            Files.copy(srcFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("📸 Đã lưu ảnh lỗi tại: " + destPath.toString());

            return destPath.toAbsolutePath().toString();
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi chụp màn hình: " + e.getMessage());
            return null;
        }
    }

    // Getter cho Driver (Hỗ trợ Listener nếu cần truy cập trực tiếp)
    public WebDriver getDriver() {
        return driver;
    }
}