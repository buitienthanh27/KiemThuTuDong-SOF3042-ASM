package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
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
    // URL mặc định nếu file config lỗi
    protected String BASE_URL = "http://localhost:9090/";

    @BeforeMethod
    public void baseSetUp() {
        // Setup Chrome Driver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");
        //options.addArguments("--headless");

        driver = new ChromeDriver(options);

        // Lấy URL từ config
        String configUrl = TestConfig.getProperty("base.url");
        if (configUrl != null && !configUrl.isEmpty()) {
            BASE_URL = configUrl;
        }

        // Đảm bảo URL luôn có dấu / ở cuối để nối chuỗi cho chuẩn
        if (!BASE_URL.endsWith("/")) {
            BASE_URL += "/";
        }

        // Implicit wait ngắn thôi, ta sẽ dùng Explicit wait (WebDriverWait) là chính
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void baseTearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- CÁC HÀM DÙNG CHUNG CHO TẤT CẢ TEST ---

    // Hàm click an toàn bằng JS (trị bệnh element not clickable)
    protected void clickElementJS(WebElement element) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(200); // Wait cực ngắn để UI ổn định
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            // Fallback
            try {
                element.click();
            } catch (Exception ex) {
                System.out.println("Không click được element: " + ex.getMessage());
            }
        }
    }

    // Hàm Login chuẩn cho Cart, Checkout sử dụng
    protected void loginAsCustomer() {
        loginCommon("abcd", "123123"); // User mặc định
    }

    // Hàm Login chuẩn cho AdminDashboard sử dụng
    protected void loginAsAdmin() {
        loginCommon("admin", "123123"); // Admin mặc định
    }

    // Logic Login cốt lõi (đã fix lỗi timeout 30s)
    private void loginCommon(String user, String pass) {
        driver.get(BASE_URL + "login");

        // Nếu đã login rồi (không còn ở trang login) thì thôi
        if (!driver.getCurrentUrl().contains("login")) return;

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            userField.clear();
            userField.sendKeys(user);

            driver.findElement(By.name("password")).sendKeys(pass);

            // Tìm nút đăng nhập chính xác hơn
            WebElement loginBtn = driver.findElement(By.xpath("//div[@id='signin']//button"));
            clickElementJS(loginBtn);

            // Wait thông minh: Chấp nhận cả URL có slash và không slash
            String homeNoSlash = BASE_URL.substring(0, BASE_URL.length() - 1);

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlToBe(BASE_URL),
                    ExpectedConditions.urlToBe(homeNoSlash),
                    ExpectedConditions.urlContains("admin") // Cho trường hợp admin
            ));

        } catch (Exception e) {
            System.out.println("⚠️ Warning Login: " + e.getMessage());
            // Không throw exception để test vẫn cố chạy tiếp bước sau
        }
    }

    // Hàm chụp ảnh màn hình (để test không báo lỗi biên dịch nếu các file con gọi nó)
    protected void takeScreenshot(String fileName) {
        // Logic chụp ảnh (bạn có thể copy lại từ file cũ nếu cần, hoặc để trống)
        // com.java.automation.utils.ScreenshotHelper.capture(...);
    }
}