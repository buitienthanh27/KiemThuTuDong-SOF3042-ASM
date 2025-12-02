package com.java.selenium;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream; // Dòng này chắc có rồi, kiểm tra lại cho chắc
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayInputStream;
import java.io.File;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(ScreenshotOnFailureExtension.class)
public class EditProfileTest extends BaseSeleniumTest {

    private static final int TIMEOUT = 15; // Tăng thời gian chờ lên xíu

    public void takeScreenshot(String fileName, String pass) {
        try {
            // 1. QUAN TRỌNG: Cuộn lên đầu trang trước tiên
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            Thread.sleep(500); // Chờ cuộn xong

            // 2. Chụp ảnh dưới dạng Byte (Để đính kèm vào Allure Report)
            byte[] content = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(fileName, new ByteArrayInputStream(content));

            // 3. Lưu ảnh ra File (Để xem offline hoặc lưu vào Artifacts của Github)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fullFileName = "screenshots/ERROR_" + fileName + "_" + timestamp + ".png";

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            java.nio.file.Path destination = java.nio.file.Paths.get(fullFileName);
            java.nio.file.Files.createDirectories(destination.getParent());
            java.nio.file.Files.copy(scrFile.toPath(), destination);

            System.out.println("📸 Đã chụp ảnh và đính kèm vào Allure Report: " + fullFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToProfilePage() {
        driver.get("http://localhost:9090/login");
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        if (driver.getCurrentUrl().contains("admin")) {
            driver.get("http://localhost:9090/logout");
            driver.get("http://localhost:9090/login");
        }

        // Login nhanh
        try {
            WebElement signInTab = driver.findElement(By.xpath("//a[contains(text(), 'sign in') and @data-toggle='tab']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", signInTab);
        } catch (Exception ignored) {}

        try {
            WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customerId")));
            email.clear();
            email.sendKeys("abcd"); // User thật
            driver.findElement(By.xpath("//div[@id='signin']//input[@name='password']")).sendKeys("123123");
            WebElement btn = driver.findElement(By.xpath("//button[contains(text(), 'sign in now')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Có thể đã đăng nhập sẵn.");
        }

        driver.get("http://localhost:9090/account");
    }

    /**
     * HÀM MỚI: Mở Modal Edit Profile
     * Phải gọi hàm này trước khi nhập liệu!
     */
    private void openEditModal() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // Tìm nút "edit profile" (dựa trên HTML bạn gửi)
            // Nút này có data-target="#profile-edit"
            WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-target='#profile-edit']")));

            System.out.println("Clicking Edit Profile button...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);

            // QUAN TRỌNG: Chờ cho cái Modal hiện ra hẳn rồi mới làm tiếp
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("profile-edit")));
            Thread.sleep(500); // Chờ hiệu ứng slide down của modal

        } catch (Exception e) {
            Assertions.fail("Không mở được Modal chỉnh sửa thông tin!");
        }
    }

    @Test
    void update_profile_info_success() {
        goToProfilePage();

        // --- BƯỚC 1: MỞ MODAL ---
        openEditModal();
        // -----------------------

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String newName = "User Update " + System.currentTimeMillis();
        String newPhone = "09" + (System.currentTimeMillis() / 1000);
        String newAddress = "Dia chi moi " + System.currentTimeMillis();

        // --- BƯỚC 2: NHẬP LIỆU VÀO MODAL ---
        // Lưu ý: Tìm input bên trong modal có id='profile-edit'
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='profile-edit']//input[@name='fullname']")));
        nameInput.clear();
        nameInput.sendKeys(newName);

        driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='phone']")).clear();
        driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='phone']")).sendKeys(newPhone);

        driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='address']")).clear();
        driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='address']")).sendKeys(newAddress);

        // --- BƯỚC 3: BẤM SAVE (Trong Modal) ---
        // Nút save trong HTML của bạn là chữ thường "save change"
        WebElement saveBtn = driver.findElement(By.xpath("//div[@id='profile-edit']//button[contains(text(), 'save change')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

        // --- BƯỚC 4: KIỂM TRA ---
        try {
            // Chờ modal tắt và trang reload lại
            Thread.sleep(1000);
            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            Assertions.assertTrue(successMsg.getText().contains("thành công"));

            takeScreenshot("UpdateInfo_Success", "PASS");
        } catch (Exception e) {
            Assertions.fail("Cập nhật thất bại.");
        }
    }

    @Test
    void update_profile_avatar_success() {
        goToProfilePage();
        openEditModal(); // Mở modal trước

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            File tempImage = File.createTempFile("test-avatar", ".jpg");
            tempImage.deleteOnExit();

            // Tìm input file trong modal
            WebElement uploadInput = driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='image']"));
            uploadInput.sendKeys(tempImage.getAbsolutePath());

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='profile-edit']//button[contains(text(), 'save change')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            Assertions.assertTrue(successMsg.isDisplayed());

            takeScreenshot("UpdateAvatar_Success", "PASS");
        } catch (Exception e) {
            Assertions.fail("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @Test
    void verify_email_is_readonly() {
        goToProfilePage();
        openEditModal(); // Mở modal để thấy ô email

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='profile-edit']//input[@name='email']")));

        String originalEmail = emailInput.getAttribute("value");
        String readonlyAttr = emailInput.getAttribute("readonly");

        Assertions.assertNotNull(readonlyAttr, "LỖI BẢO MẬT: Ô Email thiếu thuộc tính readonly!");

        try {
            emailInput.sendKeys("hacker@gmail.com");
            String newEmail = emailInput.getAttribute("value");
            Assertions.assertEquals(originalEmail, newEmail, "LỖI: Vẫn sửa được email!");

            takeScreenshot("Email_Readonly_Verified", "PASS");
        } catch (Exception e) {
            takeScreenshot("Email_Readonly_Verified", "PASS");
        }
    }
}