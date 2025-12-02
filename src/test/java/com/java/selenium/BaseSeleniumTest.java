package com.java.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public abstract class BaseSeleniumTest {

    protected static WebDriver driver;
    // App đang chạy ở http://localhost:8080/
    protected static final String BASE_URL = "http://localhost:8080/";

    @BeforeAll
    static void setUpClass() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        // nếu cần có thể dùng implicit wait:
        // driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void openHomePage() {
        driver.get(BASE_URL);      // không cộng thêm gì nữa
    }
}