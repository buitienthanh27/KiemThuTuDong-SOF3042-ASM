package com.java.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseSeleniumTest {

    protected static WebDriver driver;
    protected static final String BASE_URL = "http://localhost:9090/";

    @BeforeAll
    static void setUpClass() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // --- LOGIC THÔNG MINH: TỰ NHẬN DIỆN MÔI TRƯỜNG ---
        String isCI = System.getenv("GITHUB_ACTIONS");

        if (isCI != null && "true".equalsIgnoreCase(isCI)) {
            // === CẤU HÌNH CHO GITHUB ACTIONS (SERVER LINUX) ===
            System.out.println("🤖 Đang chạy trên CI/CD (Headless Mode)...");
            options.addArguments("--headless"); // Chạy ngầm
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080"); // Set cứng kích thước ảo
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        } else {
            // === CẤU HÌNH CHO MÁY CÁ NHÂN (LOCAL) ===
            System.out.println("💻 Đang chạy trên máy Local (GUI Mode)...");
            options.addArguments("--start-maximized"); // Hiện trình duyệt to rõ
        }

        // Khởi tạo Driver
        driver = new ChromeDriver(options);

        // Đảm bảo maximize (cho chắc chắn với mọi môi trường)
        if (isCI == null) {
            driver.manage().window().maximize();
        }
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void openHomePage() {
        driver.get(BASE_URL);
    }

    // --- HÀM CHỤP ẢNH TÍCH HỢP ALLURE REPORT ---
    public void takeScreenshot(String fileName) {
        try {
            // 1. Cuộn lên đầu trang
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            Thread.sleep(500);

            // 2. Chụp ảnh cho Allure (Byte Array)
            byte[] content = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(fileName, new ByteArrayInputStream(content));

            // 3. Lưu ảnh ra File (Để xem offline nếu cần)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFileName = "screenshots/ERROR_" + fileName + "_" + timestamp + ".png";

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = Paths.get(fullFileName);
            Files.createDirectories(destination.getParent());
            Files.copy(scrFile.toPath(), destination);

            System.out.println("📸 Đã chụp ảnh lỗi: " + fullFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}