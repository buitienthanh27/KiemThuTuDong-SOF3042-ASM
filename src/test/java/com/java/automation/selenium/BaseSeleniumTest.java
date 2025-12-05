package com.java.automation.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseSeleniumTest {

    // Để static để dùng chung cho toàn bộ Suite test
    public static WebDriver driver;
    // Port 9090 theo yêu cầu của bạn
    protected static final String BASE_URL = "http://localhost:9090/";

    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // --- LOGIC NHẬN DIỆN CI/CD (GITHUB ACTIONS) ---
        String isCI = System.getenv("GITHUB_ACTIONS");

        if (isCI != null && "true".equalsIgnoreCase(isCI)) {
            System.out.println("🤖 Detect CI Environment: Running Headless Chrome");
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");
        } else {
            System.out.println("💻 Detect Local Environment: Running GUI Chrome");
            options.addArguments("--start-maximized");
        }

        driver = new ChromeDriver(options);

        if (isCI == null) {
            driver.manage().window().maximize();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Hàm hỗ trợ chụp ảnh thủ công (nếu cần dùng trong test case)
    public void takeScreenshot(String fileName) {
        try {
            // Cuộn lên đầu
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            Thread.sleep(500);

            // 1. Allure Report (Byte)
            byte[] content = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(fileName, new ByteArrayInputStream(content));

            // 2. Lưu File (Local/Artifacts)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFileName = "screenshots/" + fileName + "_" + timestamp + ".png";

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = Paths.get(fullFileName);
            Files.createDirectories(destination.getParent());
            Files.copy(scrFile.toPath(), destination);

            System.out.println("📸 Saved screenshot: " + fullFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}