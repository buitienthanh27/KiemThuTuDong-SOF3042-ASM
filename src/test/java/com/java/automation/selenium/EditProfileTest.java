package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;

@Listeners(TestListener.class)
public class EditProfileTest extends BaseSeleniumTest {

    private WebDriverWait wait;
    private LoginOrRegisterPage loginPage;
    private static final int TIMEOUT = 20; // Tăng timeout cho môi trường CI

    // Đường dẫn ảnh tĩnh để test upload
    private static final String AVATAR_PATH = System.getProperty("user.dir") + "/src/main/resources/static/images/product/02.jpg";

    @BeforeMethod
    public void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
        loginPage = new LoginOrRegisterPage(driver);
    }

    private void ensureLoggedIn() {
        loginPage.navigateToLoginPage();
        if (loginPage.isOnLoginPage()) {
            String user = TestConfig.getProperty("test.user.id"); // Dùng user thường để test profile
            String pass = TestConfig.getProperty("test.user.password");

            // Fallback nếu config chưa set
            if (user == null) user = "abcd";
            if (pass == null) pass = "123123";

            System.out.println("🔄 Login Profile User: " + user);
            loginPage.login(user, pass);
        }
    }

    private void loginAndGoToProfile() {
        ensureLoggedIn();

        // Điều hướng vào trang Profile
        driver.get(TestConfig.getBaseUrl() + "/profile"); // Thử URL /profile trước

        // Nếu không đúng URL, thử tìm link trong menu (trường hợp URL khác)
        if (!driver.getCurrentUrl().contains("profile") && !driver.getCurrentUrl().contains("account")) {
            driver.get(TestConfig.getBaseUrl() + "/account");
        }
    }

    private void openEditModal() {
        try {
            waitForPageLoaded();

            // FIX QUAN TRỌNG: Thay đổi Selector tìm nút Edit
            // Tìm thẻ 'a' chứa text 'Edit' (thay vì button[data-target])
            // Sử dụng XPath để tìm chính xác nút Edit trong phần thông tin tài khoản
            WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class,'account-title')]//a[contains(text(),'Edit')] | //a[contains(text(),'Edit') and contains(@href, 'profile')]")
            ));

            smartClick(editBtn);

            // Chờ Modal hiện ra (ID của modal thường là profile-edit)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("profile-edit")));
            Thread.sleep(500); // Chờ animation modal

        } catch (Exception e) {
            takeScreenshot("Open_Edit_Modal_Fail");
            Assert.fail("Không mở được Modal chỉnh sửa thông tin! (Kiểm tra lại Selector nút Edit): " + e.getMessage());
        }
    }

    // --- TEST CASES ---

    @Test(priority = 1)
    void update_profile_info_success() {
        loginAndGoToProfile();
        openEditModal();

        String newName = "User Update " + System.currentTimeMillis();
        String newPhone = "09" + (System.currentTimeMillis() / 100000);
        String newAddress = "Dia chi moi " + System.currentTimeMillis();

        try {
            // Nhập tên
            WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='profile-edit']//input[@name='fullname']")));
            nameInput.clear();
            nameInput.sendKeys(newName);

            // Nhập SĐT
            driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='phone']")).clear();
            driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='phone']")).sendKeys(newPhone);

            // Nhập Địa chỉ
            driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='address']")).clear();
            driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='address']")).sendKeys(newAddress);

            // Click Save
            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='profile-edit']//button[contains(text(), 'save') or contains(text(), 'Save')]"));
            smartClick(saveBtn);

            // Kiểm tra thành công
            Thread.sleep(1500);

            // Cách 1: Check alert success
            boolean isSuccess = false;
            try {
                if(driver.findElement(By.cssSelector(".alert-success")).isDisplayed()) isSuccess = true;
            } catch (Exception ignored) {}

            // Cách 2: Check dữ liệu hiển thị sau khi reload (chắc chắn hơn)
            if (!isSuccess) {
                driver.navigate().refresh();
                waitForPageLoaded();
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
            File avatar = new File(AVATAR_PATH);
            if (!avatar.exists()) {
                System.out.println("⚠️ Không tìm thấy ảnh test avatar, bỏ qua test này.");
                return;
            }

            // Upload input thường bị ẩn, cần sendKeys trực tiếp
            WebElement uploadInput = driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='image']"));
            uploadInput.sendKeys(avatar.getAbsolutePath());

            // Click Save
            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='profile-edit']//button[contains(text(), 'save') or contains(text(), 'Save')]"));
            smartClick(saveBtn);

            Thread.sleep(2000); // Chờ upload

            // Verify
            try {
                // Check alert hoặc check modal đóng
                boolean modalClosed = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("profile-edit")));
                Assert.assertTrue(modalClosed, "Modal không đóng sau khi save -> Có thể lỗi server.");
                System.out.println("✅ Upload Avatar thành công.");
            } catch (Exception e) {
                Assert.fail("Upload thất bại hoặc timeout.");
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

            // Nếu không có readonly, thử nhập liệu xem có đổi được không
            if (readonlyAttr == null) {
                emailInput.sendKeys("hack@test.com");
                String newEmail = emailInput.getAttribute("value");

                // Nếu value thay đổi -> Lỗi bảo mật
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