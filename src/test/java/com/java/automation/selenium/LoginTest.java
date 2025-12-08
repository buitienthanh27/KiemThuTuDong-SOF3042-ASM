package com.java.automation.selenium;

import com.java.automation.config.TestConfig;
import com.java.automation.pages.LoginOrRegisterPage;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public class LoginTest extends BaseSeleniumTest {

    // --- TEST CASE 1: LOGIN THÀNH CÔNG ---
    @Test(priority = 1, description = "Test đăng nhập thành công với tài khoản thật")
    public void login_with_valid_customer_should_success() {
        System.out.println("--- BẮT ĐẦU TEST CASE: LOGIN SUCCESS ---");

        // 1. Khởi tạo Page Object
        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);

        // 2. Vào trang login (Hàm này đã xử lý logout nếu cần)
        loginPage.navigateToLoginPage();

        // 3. Lấy data từ test.properties (User: abcd / Pass: 123123)
        // Đảm bảo file test.properties của bạn có: test.user.id=abcd và test.user.password=123123
        String userId = TestConfig.getProperty("test.user.id");
        String password = TestConfig.getProperty("test.user.password");

        if (userId == null || password == null) {
            Assert.fail("Lỗi Config: Không tìm thấy test.user.id hoặc test.user.password trong file properties!");
        }

        System.out.println("Login với user: " + userId);

        // 4. Thực hiện login (Hàm này đã lo việc tìm element, wait, click...)
        loginPage.login(userId, password);

        // 5. Kiểm tra kết quả
        // Hàm isOnHomePage đã có wait thông minh để chờ redirect
        boolean isHome = loginPage.isOnHomePage();

        if (isHome) {
            System.out.println("✅ Login thành công: URL đã về trang chủ.");
        } else {
            takeScreenshot("Login_Valid_FAIL");
            Assert.fail("Login thất bại: Không chuyển về trang chủ sau khi đăng nhập!");
        }
    }

    // --- TEST CASE 2: LOGIN SAI PASS ---
    @Test(priority = 2, description = "Test đăng nhập thất bại với mật khẩu sai")
    public void login_with_wrong_password_should_show_error() {
        System.out.println("--- BẮT ĐẦU TEST CASE: LOGIN WRONG PASS ---");

        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();

        String userId = TestConfig.getProperty("test.user.id");
        // Nhập mật khẩu sai
        loginPage.login(userId, "wrong_pass_123");

        // Kiểm tra phải hiện lỗi hoặc vẫn ở trang login
        // isErrorAlertDisplayed() sẽ chờ thông báo lỗi xuất hiện
        boolean isError = loginPage.isErrorAlertDisplayed();
        boolean stillAtLogin = loginPage.isOnLoginPage(); // Hàm này check URL có chứa 'login' không

        if (isError || stillAtLogin) {
            System.out.println("✅ Pass: Hệ thống chặn login sai pass.");
            takeScreenshot("Login_WrongPass_Blocked");
        } else {
            takeScreenshot("Login_WrongPass_FAIL");
            Assert.fail("Lỗi: Nhập sai pass mà không báo lỗi hoặc tự vào trang chủ!");
        }
    }

    // --- TEST CASE 3: LOGIN USER KHÔNG TỒN TẠI ---
    @Test(priority = 3, description = "Test đăng nhập thất bại với tài khoản không tồn tại")
    public void login_fail_user_not_exist() {
        System.out.println("--- BẮT ĐẦU TEST CASE: LOGIN USER NOT EXIST ---");

        LoginOrRegisterPage loginPage = new LoginOrRegisterPage(driver);
        loginPage.navigateToLoginPage();

        // Nhập user ảo
        loginPage.login("tai_khoan_ma_" + System.currentTimeMillis(), "123456");

        // Kiểm tra logic chặn
        // Chỉ cần KHÔNG vào được trang chủ là coi như Pass (vì có thể nó chỉ reload trang mà không hiện lỗi rõ ràng)
        boolean notInHomePage = !loginPage.isOnHomePage();

        if (notInHomePage) {
            System.out.println("✅ Pass: Hệ thống không cho vào trang chủ.");
            takeScreenshot("Login_NotExist_Blocked");
        } else {
            takeScreenshot("Login_NotExist_FAIL");
            Assert.fail("Lỗi nghiêm trọng: Tài khoản ma mà vẫn vào được trang chủ!");
        }
    }
}