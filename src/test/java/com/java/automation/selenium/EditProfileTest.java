package com.java.automation.selenium;

import com.java.automation.selenium.BaseSeleniumTest;
import com.java.automation.selenium.TestListener;
import io.qameta.allure.Allure;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Listeners(TestListener.class)
public class EditProfileTest extends BaseSeleniumTest {

    private static final int TIMEOUT = 15;

    // Dùng hàm chụp ảnh của BaseSeleniumTest (hoặc override nếu muốn custom)
    @Override
    public void takeScreenshot(String fileName) {
        super.takeScreenshot(fileName);
    }

    private void goToProfilePage() {
        driver.get(BASE_URL + "login");
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        if (driver.getCurrentUrl().contains("admin")) {
            driver.get(BASE_URL + "logout");
            driver.get(BASE_URL + "login");
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

        driver.get(BASE_URL + "account");
    }

    private void openEditModal() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-target='#profile-edit']")));

            System.out.println("Clicking Edit Profile button...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("profile-edit")));
            Thread.sleep(500);

        } catch (Exception e) {
            Assert.fail("Không mở được Modal chỉnh sửa thông tin!");
        }
    }

    @Test(priority = 1)
    void update_profile_info_success() {
        goToProfilePage();
        openEditModal();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String newName = "User Update " + System.currentTimeMillis();
        String newPhone = "09" + (System.currentTimeMillis() / 1000);
        String newAddress = "Dia chi moi " + System.currentTimeMillis();

        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='profile-edit']//input[@name='fullname']")));
        nameInput.clear();
        nameInput.sendKeys(newName);

        driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='phone']")).clear();
        driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='phone']")).sendKeys(newPhone);

        driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='address']")).clear();
        driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='address']")).sendKeys(newAddress);

        WebElement saveBtn = driver.findElement(By.xpath("//div[@id='profile-edit']//button[contains(text(), 'save change')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

        try {
            Thread.sleep(1000);
            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            Assert.assertTrue(successMsg.getText().contains("thành công"));

            takeScreenshot("UpdateInfo_Success");
        } catch (Exception e) {
            Assert.fail("Cập nhật thất bại.");
        }
    }

    @Test(priority = 2)
    void update_profile_avatar_success() {
        goToProfilePage();
        openEditModal();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // Tạo file giả để upload
            File tempImage = File.createTempFile("test-avatar", ".jpg");
            tempImage.deleteOnExit();

            WebElement uploadInput = driver.findElement(By.xpath("//div[@id='profile-edit']//input[@name='image']"));
            uploadInput.sendKeys(tempImage.getAbsolutePath());

            WebElement saveBtn = driver.findElement(By.xpath("//div[@id='profile-edit']//button[contains(text(), 'save change')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
            Assert.assertTrue(successMsg.isDisplayed());

            takeScreenshot("UpdateAvatar_Success");
        } catch (Exception e) {
            Assert.fail("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @Test(priority = 3)
    void verify_email_is_readonly() {
        goToProfilePage();
        openEditModal();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='profile-edit']//input[@name='email']")));

        String originalEmail = emailInput.getAttribute("value");
        String readonlyAttr = emailInput.getAttribute("readonly");

        Assert.assertNotNull(readonlyAttr, "LỖI BẢO MẬT: Ô Email thiếu thuộc tính readonly!");

        try {
            emailInput.sendKeys("hacker@gmail.com");
            String newEmail = emailInput.getAttribute("value");
            Assert.assertEquals(originalEmail, newEmail, "LỖI: Vẫn sửa được email!");

            takeScreenshot("Email_Readonly_Verified");
        } catch (Exception e) {
            takeScreenshot("Email_Readonly_Verified");
        }
    }
}