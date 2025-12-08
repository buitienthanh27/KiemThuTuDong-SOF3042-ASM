package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;

import static com.java.automation.utils.ScreenshotUtil.takeScreenshot;

@Listeners(TestListener.class)
public class EditProfileTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private static final int TIMEOUT = 5; // Tăng timeout cho CI

    // Đường dẫn ảnh tĩnh để test upload (tránh tạo file rác)
    private static final String AVATAR_PATH = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "images" + File.separator + "product" + File.separator + "02.jpg";

    @BeforeMethod
    void setUpTest() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    // --- HELPER METHODS ---

    public void clickElementJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    private void loginAndGoToProfile() {
        // 1. Đăng nhập chuẩn bằng Page Object
        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();

        String userId = TestConfig.getProperty("test.user.id");
        String password = TestConfig.getProperty("test.user.password");

        System.out.println("🔄 Login Profile User: " + userId);
        loginPage.login(userId, password);

        // 2. Vào trang Profile
        driver.get(BASE_URL + "account");

        // 3. Đảm bảo đã vào được trang Account
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("account"),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Profile')] | //h4[contains(text(), 'Profile')]"))
            ));
        } catch (Exception e) {
            Assert.fail("Không thể truy cập trang Profile. Có thể login thất bại.");
        }
    }

    private void openEditModal() {
        try {
            // Tìm nút Edit (thường là button hoặc a có data-target)
            WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-target='#profile-edit']")));

            clickElementJS(editBtn);

            // Chờ Modal hiện ra
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("profile-edit")));
            Thread.sleep(500); // Chờ animation modal

        } catch (Exception e) {
            takeScreenshot("Open_Edit_Modal_Fail");
            Assert.fail("Không mở được Modal chỉnh sửa thông tin! Lỗi: " + e.getMessage());
        }
    }

    // --- TEST CASES ---

    @Test(priority = 1)
    void update_profile_info_success() {
        loginAndGoToProfile();
        openEditModal();

        String newName = "User Update " + System.currentTimeMillis();
        String newPhone = "09" + (System.currentTimeMillis() / 1000);
        String newAddress = "Dia chi moi " + System.currentTimeMillis();

        try {
            // Nhập tên
            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='profile-edit']//input[@name='fullname']")));
            nameInput.clear();
            nameInput.sendKeys(newName);

            // Nhập SĐT
            WebElement phoneInput = driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='phone']"));
            phoneInput.clear();
            phoneInput.sendKeys(newPhone);

            // Nhập Địa chỉ
            WebElement addrInput = driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='address']"));
            addrInput.clear();
            addrInput.sendKeys(newAddress);

            // Click Save
            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='profile-edit']//button[contains(text(), 'save change')]"));
            clickElementJS(saveBtn);

            // Kiểm tra thành công (Alert hoặc reload trang)
            Thread.sleep(1500);
            boolean isSuccess = false;

            try {
                // Check thông báo thành công
                WebElement successMsg = driver.findElement(By.cssSelector(".alert-success"));
                if(successMsg.isDisplayed()) isSuccess = true;
            } catch (Exception ignored) {}

            // Hoặc check xem dữ liệu trên trang đã đổi chưa
            if (!isSuccess) {
                driver.navigate().refresh();
                if(driver.getPageSource().contains(newName)) isSuccess = true;
            }

            Assert.assertTrue(isSuccess, "Cập nhật thông tin thất bại!");
            System.out.println("✅ Cập nhật thông tin thành công.");

        } catch (Exception e) {
            takeScreenshot("UpdateInfo_Fail");
            Assert.fail("Lỗi cập nhật: " + e.getMessage());
        }
    }

    @Test(priority = 2)
    void update_profile_avatar_success() {
        loginAndGoToProfile();
        openEditModal();

        try {
            // Upload ảnh (Sử dụng ảnh có sẵn trong project thay vì tạo temp file)
            File avatar = new File(AVATAR_PATH);
            if (!avatar.exists()) {
                System.out.println("⚠️ Không tìm thấy ảnh test avatar: " + AVATAR_PATH);
                // Bỏ qua test này nếu không có ảnh, tránh fail oan
                return;
            }

            WebElement uploadInput = driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='image']"));
            uploadInput.sendKeys(avatar.getAbsolutePath());

            // Click Save
            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='profile-edit']//button[contains(text(), 'save change')]"));
            clickElementJS(saveBtn);

            // Check Success
            Thread.sleep(1500);
            try {
                WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
                Assert.assertTrue(successMsg.isDisplayed());
                System.out.println("✅ Upload Avatar thành công.");
            } catch (Exception e) {
                // Nếu không có alert, thử check xem modal đóng chưa
                if(driver.findElements(By.id("profile-edit")).isEmpty() || !driver.findElement(By.id("profile-edit")).isDisplayed()) {
                    System.out.println("✅ Upload xong, modal đã đóng.");
                } else {
                    Assert.fail("Upload thất bại, không thấy thông báo.");
                }
            }

        } catch (Exception e) {
            takeScreenshot("UpdateAvatar_Fail");
            Assert.fail("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    void verify_email_is_readonly() {
        loginAndGoToProfile();
        openEditModal();

        try {
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='profile-edit']//input[@name='email']")));

            String originalEmail = emailInput.getAttribute("value");

            // Check thuộc tính readonly
            String readonlyAttr = emailInput.getAttribute("readonly");
            if (readonlyAttr == null) {
                // Thử nhập liệu để kiểm chứng thực tế
                emailInput.sendKeys("hacker@gmail.com");
                String newEmail = emailInput.getAttribute("value");

                if (!originalEmail.equals(newEmail)) {
                    takeScreenshot("Email_Readonly_Fail");
                    Assert.fail("LỖI BẢO MẬT: Ô Email cho phép chỉnh sửa!");
                }
            }

            System.out.println("✅ Email field is Read-only.");

        } catch (Exception e) {
            takeScreenshot("Email_Readonly_Error");
            Assert.fail("Lỗi kiểm tra email: " + e.getMessage());
        }
    }
}