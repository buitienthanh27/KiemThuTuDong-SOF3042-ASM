# 🚀 Vegana Shop Test Automation Framework

Framework test automation chuyên nghiệp sử dụng **Selenium 3**, **TestNG**, **Extent Reports**, và **Log4j** cho chức năng đăng nhập và đăng ký.

---

## 📋 Mục lục

- [Tổng quan](#-tổng-quan)
- [Tính năng nổi bật](#-tính-năng-nổi-bật)
- [Cấu trúc Project](#-cấu-trúc-project)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt](#-cài-đặt)
- [Chạy Tests](#-chạy-tests)
- [Test Cases](#-test-cases)
- [Báo cáo và Logging](#-báo-cáo-và-logging)
- [Tính năng nâng cao](#-tính-năng-nâng-cao)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Tổng quan

Framework này được xây dựng dựa trên các best practices của Selenium 3, bao gồm:

- ✅ **Page Object Model (POM)** - Tách biệt logic test và UI elements
- ✅ **TestNG Framework** - Quản lý test cases, data-driven testing
- ✅ **Extent Reports** - Báo cáo HTML đẹp mắt với screenshots
- ✅ **Log4j2** - Logging chi tiết từng bước
- ✅ **Screenshot tự động** - Chụp ảnh khi test fail
- ✅ **DataProvider** - Test với nhiều bộ dữ liệu
- ✅ **WebDriverManager** - Tự động quản lý browser drivers

---

## ⭐ Tính năng nổi bật

### 1. **Page Object Model (POM) + Page Factory**

Tách biệt hoàn toàn logic test và UI elements, giúp code dễ maintain và reuse.

```java
@FindBy(name = "customerId")
private WebElement loginCustomerIdInput;

public void login(String customerId, String password) {
    enterLoginCustomerId(customerId);
    enterLoginPassword(password);
    clickSignInButton();
}
```

### 2. **TestNG DataProvider - Data-Driven Testing**

Chạy test với nhiều bộ dữ liệu khác nhau một cách dễ dàng.

```java
@DataProvider(name = "loginData")
public Object[][] getLoginData() {
    return new Object[][] {
        {"user1", "pass1", "success"},
        {"user2", "pass2", "error"}
    };
}

@Test(dataProvider = "loginData")
public void testLogin(String user, String pass, String expected) {
    // Test logic
}
```

### 3. **Extent Reports - Báo cáo HTML chuyên nghiệp**

- 📊 Dashboard tổng quan
- 📸 Screenshots tự động khi test fail
- 📈 Charts và statistics
- 🔍 Chi tiết từng test case

**Xem báo cáo tại:** `test-output/reports/ExtentReport_*.html`

### 4. **Log4j2 - Logging chi tiết**

- Console logging
- File logging với rotation
- Log levels: INFO, DEBUG, ERROR, WARN

**Xem logs tại:** `test-output/logs/automation.log`

### 5. **Screenshot tự động khi test fail**

Tự động chụp ảnh màn hình khi test thất bại và đính kèm vào báo cáo.

**Screenshots lưu tại:** `test-output/screenshots/`

### 6. **Smart Waits - ExpectedConditions**

Sử dụng WebDriverWait với ExpectedConditions để tránh flaky tests.

```java
wait.until(ExpectedConditions.visibilityOf(element));
wait.until(ExpectedConditions.elementToBeClickable(button));
```

---

## 📁 Cấu trúc Project

```
src/test/java/com/java/automation/
├── base/
│   └── BaseTest.java                    # Base class với WebDriver, Reports, Logging
├── config/
│   └── TestConfig.java                  # Configuration class
├── pages/
│   └── LoginOrRegisterPage.java         # Page Object Model
├── tests/
│   └── user/
│       ├── LoginTest.java               # Test cases đăng nhập
│       ├── LoginTestWithDataProvider.java  # Data-driven login tests
│       └── RegisterTest.java            # Test cases đăng ký
└── utils/
    ├── ExtentReportManager.java         # Extent Reports manager
    ├── LoggerUtil.java                  # Logging utility
    ├── ScreenshotUtil.java              # Screenshot utility
    └── TestDataGenerator.java           # Test data generator

src/test/resources/
├── log4j2.xml                          # Log4j configuration
├── test.properties                      # Test configuration
└── testng.xml                          # TestNG suite configuration

test-output/
├── reports/                            # Extent Reports HTML files
├── screenshots/                        # Screenshots khi test fail
└── logs/                               # Log files
```

---

## 💻 Yêu cầu hệ thống

- **Java**: JDK 8+ (khuyến nghị JDK 17)
- **Maven**: 3.6+
- **Browser**: Chrome, Firefox, hoặc Edge
- **Spring Boot Application**: Đang chạy tại `http://localhost:9090`

---

## 🔧 Cài đặt

### 1. Clone project và cài đặt dependencies

```bash
# Clone project
git clone <repository-url>
cd Vegana-shop

# Cài đặt dependencies
mvn clean install
```

### 2. Cấu hình test properties

Chỉnh sửa file `src/test/resources/test.properties`:

```properties
# Base URL của ứng dụng
base.url=http://localhost:9090

# Browser để chạy test (chrome, firefox, edge)
browser=chrome

# Timeouts
implicit.wait=10
page.load.timeout=30

# Test Data
test.user.id=testuser001
test.user.password=123456
test.user.fullname=Test User
test.user.email=testuser001@example.com
```

### 3. Đảm bảo ứng dụng đang chạy

```bash
# Chạy Spring Boot application
mvn spring-boot:run
```

---

## 🏃 Chạy Tests

### Chạy tất cả tests

```bash
mvn test
```

### Chạy test suite với TestNG

```bash
mvn test -DsuiteXmlFile=src/test/resources/testng.xml
```

### Chạy test class cụ thể

```bash
# Chạy LoginTest
mvn test -Dtest=LoginTest

# Chạy RegisterTest
mvn test -Dtest=RegisterTest

# Chạy LoginTestWithDataProvider
mvn test -Dtest=LoginTestWithDataProvider
```

### Chạy test method cụ thể

```bash
mvn test -Dtest=LoginTest#testLoginSuccess
```

### Chạy test với browser khác

Chỉnh sửa `test.properties`:
```properties
browser=firefox  # hoặc chrome, edge
```

---

## 📝 Test Cases

### Login Tests (LoginTest.java)

| # | Test Case | Mô tả |
|---|-----------|-------|
| 1 | `testLoginSuccess` | Đăng nhập thành công với thông tin hợp lệ |
| 2 | `testLoginWithInvalidCustomerId` | Đăng nhập thất bại với Customer ID sai |
| 3 | `testLoginWithInvalidPassword` | Đăng nhập thất bại với mật khẩu sai |
| 4 | `testLoginWithEmptyFields` | Đăng nhập thất bại với các trường trống |
| 5 | `testLoginWithRememberMe` | Đăng nhập với Remember Me checkbox |
| 6 | `testLoginWithEmptyCustomerId` | Đăng nhập với Customer ID trống |
| 7 | `testLoginWithEmptyPassword` | Đăng nhập với mật khẩu trống |

### Register Tests (RegisterTest.java)

| # | Test Case | Mô tả |
|---|-----------|-------|
| 1 | `testRegisterSuccess` | Đăng ký thành công với thông tin hợp lệ |
| 2 | `testRegisterWithExistingCustomerId` | Đăng ký thất bại với Customer ID đã tồn tại |
| 3 | `testRegisterWithExistingEmail` | Đăng ký thất bại với Email đã tồn tại |
| 4 | `testRegisterWithEmptyCustomerId` | Đăng ký với Customer ID trống |
| 5 | `testRegisterWithEmptyFullname` | Đăng ký với Fullname trống |
| 6 | `testRegisterWithEmptyEmail` | Đăng ký với Email trống |
| 7 | `testRegisterWithInvalidEmail` | Đăng ký với Email không hợp lệ |
| 8 | `testRegisterWithEmptyPassword` | Đăng ký với mật khẩu trống |
| 9 | `testRegisterWithShortPassword` | Đăng ký với mật khẩu quá ngắn (< 6 ký tự) |
| 10 | `testSwitchBetweenTabs` | Chuyển đổi giữa tab Sign In và Sign Up |

### Data-Driven Tests (LoginTestWithDataProvider.java)

Test đăng nhập với nhiều bộ dữ liệu khác nhau sử dụng TestNG DataProvider.

---

## 📊 Báo cáo và Logging

### Extent Reports

Sau khi chạy tests, mở file báo cáo HTML:

```
test-output/reports/ExtentReport_YYYYMMDD_HHMMSS.html
```

**Tính năng:**
- 📊 Dashboard với tổng quan kết quả
- 📈 Charts và statistics
- 📸 Screenshots tự động khi test fail
- 🔍 Chi tiết từng test case với logs
- 📋 System information

### Log4j2 Logs

**Console Logging:**
- Hiển thị real-time trong console khi chạy tests

**File Logging:**
- `test-output/logs/automation.log` - Log file chính
- `test-output/logs/automation-rolling.log` - Rolling log file

**Log Levels:**
- `INFO` - Thông tin chung
- `DEBUG` - Chi tiết debug
- `ERROR` - Lỗi
- `WARN` - Cảnh báo

### Screenshots

Screenshots được tự động chụp khi test fail và lưu tại:
```
test-output/screenshots/
```

---

## 🚀 Tính năng nâng cao

### 1. Page Object Model Pattern

Tách biệt hoàn toàn UI elements và test logic:

```java
// Page Object
public class LoginOrRegisterPage {
    @FindBy(name = "customerId")
    private WebElement loginCustomerIdInput;
    
    public void login(String customerId, String password) {
        // Implementation
    }
}

// Test Class
@Test
public void testLogin() {
    loginPage.login("user", "pass");
}
```

### 2. TestNG DataProvider

Chạy test với nhiều bộ dữ liệu:

```java
@DataProvider(name = "loginData")
public Object[][] getLoginData() {
    return new Object[][] {
        {"user1", "pass1", "success"},
        {"user2", "pass2", "error"}
    };
}

@Test(dataProvider = "loginData")
public void testLogin(String user, String pass, String expected) {
    // Test logic
}
```

### 3. Smart Waits

Sử dụng ExpectedConditions để chờ elements:

```java
wait.until(ExpectedConditions.visibilityOf(element));
wait.until(ExpectedConditions.elementToBeClickable(button));
wait.until(ExpectedConditions.presenceOfElementLocated(By.id("id")));
```

### 4. Screenshot tự động

Tự động chụp ảnh khi test fail:

```java
// Tự động trong BaseTest.afterMethod()
if (result.getStatus() == ITestResult.FAILURE) {
    String screenshotPath = ScreenshotUtil.takeScreenshot(driver, testName);
    extentTest.addScreenCaptureFromPath(screenshotPath);
}
```

### 5. Logging chi tiết

Log từng bước trong test:

```java
logger.info("Starting test: " + testName);
logger.info("Navigated to: " + url);
logger.error("Test failed: " + errorMessage);
```

---

## 🔍 Troubleshooting

### Lỗi: "WebDriver not found"

**Giải pháp:**
- WebDriverManager sẽ tự động download driver
- Đảm bảo có kết nối internet
- Kiểm tra version browser đã cài đặt

### Lỗi: "Connection refused"

**Giải pháp:**
- Kiểm tra ứng dụng Spring Boot đang chạy tại `http://localhost:9090`
- Kiểm tra port trong `test.properties`
- Kiểm tra firewall settings

### Lỗi: "Element not found"

**Giải pháp:**
- Kiểm tra selectors trong `LoginOrRegisterPage.java`
- Đảm bảo HTML structure không thay đổi
- Tăng timeout trong `test.properties`

### Lỗi: "Screenshot not saved"

**Giải pháp:**
- Kiểm tra quyền ghi file trong thư mục `test-output/screenshots/`
- Đảm bảo đủ dung lượng ổ cứng

### Test chạy chậm

**Giải pháp:**
- Giảm `implicit.wait` trong `test.properties`
- Sử dụng explicit waits thay vì implicit waits
- Chạy tests song song với TestNG parallel execution

---

## 📚 Tài liệu tham khảo

- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [TestNG Documentation](https://testng.org/doc/documentation-main.html)
- [Extent Reports](https://www.extentreports.com/)
- [Log4j2 Documentation](https://logging.apache.org/log4j/2.x/)

---

## 🤝 Đóng góp

Nếu bạn muốn đóng góp cho project:

1. Fork project
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

## 👨‍💻 Tác giả

Framework được tạo cho dự án **Vegana Shop Automation Testing**.

---

## 🎉 Kết luận

Framework này cung cấp một giải pháp test automation hoàn chỉnh với:

- ✅ **Page Object Model** - Code dễ maintain
- ✅ **TestNG** - Quản lý test cases mạnh mẽ
- ✅ **Extent Reports** - Báo cáo đẹp mắt
- ✅ **Log4j2** - Logging chi tiết
- ✅ **Screenshots** - Debug dễ dàng
- ✅ **DataProvider** - Data-driven testing
- ✅ **Smart Waits** - Tránh flaky tests

**Happy Testing! 🚀**
