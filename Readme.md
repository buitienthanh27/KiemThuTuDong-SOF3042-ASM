# 🛒 Vegana Shop - Automation Testing Framework

Framework test automation chuyên nghiệp cho ứng dụng **Vegana Shop** sử dụng **Selenium WebDriver 4.x**, **TestNG**, **Extent Reports**, và **Log4j2**. Framework được tích hợp với **CI/CD** qua **Jenkins** và **GitHub Actions**.

---

## 📋 Mục lục

- [Tổng quan](#-tổng-quan)
- [Tính năng nổi bật](#-tính-năng-nổi-bật)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Cấu trúc Project](#-cấu-trúc-project)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Chạy Tests](#-chạy-tests)
- [Test Cases](#-test-cases)
- [Báo cáo và Logging](#-báo-cáo-và-logging)
- [CI/CD Integration](#-cicd-integration)
- [Troubleshooting](#-troubleshooting)
- [Đóng góp](#-đóng-góp)
- [License](#-license)

---

## 🎯 Tổng quan

Framework này được xây dựng dựa trên các best practices của Selenium WebDriver 4.x và TestNG, cung cấp giải pháp test automation hoàn chỉnh cho ứng dụng web **Vegana Shop** (Spring Boot application). Framework hỗ trợ:

- ✅ **Page Object Model (POM)** - Tách biệt logic test và UI elements
- ✅ **TestNG Framework** - Quản lý test cases, data-driven testing
- ✅ **Extent Reports** - Báo cáo HTML đẹp mắt với screenshots
- ✅ **Log4j2** - Logging chi tiết từng bước
- ✅ **Screenshot tự động** - Chụp ảnh khi test fail
- ✅ **DataProvider** - Test với nhiều bộ dữ liệu
- ✅ **CI/CD Integration** - Jenkins và GitHub Actions
- ✅ **Headless Mode** - Tự động chuyển sang headless khi chạy trên CI/CD

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

- 📊 Dashboard tổng quan với charts và statistics
- 📸 Screenshots tự động khi test fail
- 🔍 Chi tiết từng test case với logs
- 📋 System information

**Xem báo cáo tại:** `test-output/reports/ExtentReport_*.html`

### 4. **Log4j2 - Logging chi tiết**

- Console logging real-time
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

### 7. **CI/CD Integration**

- **Jenkins Pipeline** - Tự động build, start app, và chạy tests
- **GitHub Actions** - CI/CD pipeline với MySQL setup và test execution
- **Headless Mode** - Tự động phát hiện và chạy headless trên CI/CD

---

## 🛠️ Công nghệ sử dụng

### Core Framework
- **Java**: JDK 17
- **Maven**: 3.6+
- **Selenium WebDriver**: 4.15.0
- **TestNG**: 7.8.0

### Reporting & Logging
- **Extent Reports**: 4.1.7
- **Log4j2**: 2.17.2

### Utilities
- **Apache Commons IO**: 2.11.0
- **Apache POI**: 4.1.0

### Application Under Test
- **Spring Boot**: 3.2.0
- **MySQL**: 8.0
- **Thymeleaf**: Template engine

---

## 📁 Cấu trúc Project

```
Vegana-Automation-Testing/
├── .github/
│   └── workflows/
│       └── github-actions-ci-cd-demo.yml    # GitHub Actions CI/CD pipeline
├── docs/
│   ├── GITHUB_ACTIONS_CI_CD_SETUP.md       # GitHub Actions setup guide
│   └── TEST_README.md                       # Test framework documentation
├── src/
│   ├── main/
│   │   ├── java/com/java/                   # Spring Boot application source
│   │   └── resources/
│   │       ├── application.properties       # Application configuration
│   │       ├── static/                      # Static resources (CSS, JS, images)
│   │       └── templates/                   # Thymeleaf templates
│   └── test/
│       ├── java/com/java/automation/
│       │   ├── base/
│       │   │   └── BaseTest.java            # Base test class với WebDriver setup
│       │   ├── config/
│       │   │   └── TestConfig.java           # Test configuration
│       │   ├── pages/
│       │   │   └── LoginOrRegisterPage.java # Page Object Model
│       │   ├── tests/
│       │   │   └── user/
│       │   │       ├── LoginTest.java                    # Login test cases
│       │   │       ├── LoginTestWithDataProvider.java    # Data-driven login tests
│       │   │       └── RegisterTest.java                 # Register test cases
│       │   └── utils/
│       │       ├── ExtentReportManager.java  # Extent Reports manager
│       │       ├── LoggerUtil.java           # Logging utility
│       │       ├── ScreenshotUtil.java       # Screenshot utility
│       │       └── TestDataGenerator.java    # Test data generator
│       └── resources/
│           ├── log4j2.xml                    # Log4j2 configuration
│           ├── test.properties               # Test configuration
│           └── testng.xml                    # TestNG suite configuration
├── test-output/
│   ├── reports/                              # Extent Reports HTML files
│   ├── screenshots/                          # Screenshots khi test fail
│   └── logs/                                 # Log files
├── target/
│   └── surefire-reports/                     # Maven Surefire reports
├── upload/
│   └── image/                                # Uploaded images
├── Jenkinsfile                               # Jenkins pipeline configuration
├── pom.xml                                   # Maven dependencies
├── vegana.sql                                # Database schema
└── README.md                                 # This file
```

---

## 💻 Yêu cầu hệ thống

### Development Environment
- **Java**: JDK 17+ (khuyến nghị JDK 17)
- **Maven**: 3.6+
- **Browser**: Chrome, Firefox, hoặc Edge (latest version)
- **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code

### Application Requirements
- **Spring Boot Application**: Đang chạy tại `http://localhost:9090`
- **MySQL Database**: 8.0+ (database: `vegana_store`)
- **Port**: 9090 (có thể thay đổi trong `application.properties`)

### CI/CD Requirements
- **Jenkins**: 2.0+ (cho Jenkins pipeline)
- **GitHub**: Repository với GitHub Actions enabled

---

## 🔧 Cài đặt

### 1. Clone project và cài đặt dependencies

```bash
# Clone project
git clone <repository-url>
cd Vegana-Automation-Testing

# Cài đặt dependencies
mvn clean install
```

### 2. Setup Database

```bash
# Tạo database
mysql -u root -p
CREATE DATABASE vegana_store;

# Import schema
mysql -u root -p vegana_store < vegana.sql
```

### 3. Cấu hình Application

Chỉnh sửa file `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/vegana_store
spring.datasource.username=root
spring.datasource.password=123456

# Server Port
server.port=9090
```

### 4. Cấu hình Test Properties

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

# Registration Test Data
register.user.id=registeruser001
register.user.password=123456
register.user.fullname=Register User
register.user.email=registeruser001@example.com
```

### 5. Đảm bảo ứng dụng đang chạy

```bash
# Chạy Spring Boot application
mvn spring-boot:run

# Hoặc chạy từ IDE
# Run VeganaShopApplication.java
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

Chỉnh sửa `src/test/resources/test.properties`:

```properties
browser=firefox  # hoặc chrome, edge
```

### Chạy test từ IDE

1. **IntelliJ IDEA**:
   - Right-click vào test class hoặc method
   - Chọn "Run" hoặc "Debug"

2. **Eclipse**:
   - Right-click vào test class
   - Chọn "Run As" > "TestNG Test"

---

## 📝 Test Cases

### Login Tests (`LoginTest.java`)

| # | Test Case | Mô tả |
|---|-----------|-------|
| 1 | `testLoginSuccess` | Đăng nhập thành công với thông tin hợp lệ |
| 2 | `testLoginWithInvalidCustomerId` | Đăng nhập thất bại với Customer ID sai |
| 3 | `testLoginWithInvalidPassword` | Đăng nhập thất bại với mật khẩu sai |
| 4 | `testLoginWithEmptyFields` | Đăng nhập thất bại với các trường trống |
| 5 | `testLoginWithRememberMe` | Đăng nhập với Remember Me checkbox |
| 6 | `testLoginWithEmptyCustomerId` | Đăng nhập với Customer ID trống |
| 7 | `testLoginWithEmptyPassword` | Đăng nhập với mật khẩu trống |

### Register Tests (`RegisterTest.java`)

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

### Data-Driven Tests (`LoginTestWithDataProvider.java`)

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

### Maven Surefire Reports

TestNG reports được tạo bởi Maven Surefire Plugin:
```
target/surefire-reports/
```

---

## 🚀 CI/CD Integration

### GitHub Actions

Framework được tích hợp với **GitHub Actions** để tự động chạy tests khi có push hoặc pull request.

**Workflow File:** `.github/workflows/github-actions-ci-cd-demo.yml`

**Tính năng:**
- ✅ Setup MySQL database (vegana_store)
- ✅ Import database schema từ vegana.sql
- ✅ Start Spring Boot application
- ✅ Run TestNG automation tests (Login Tests & Register Tests)
- ✅ Upload test reports (Extent Reports, Screenshots, Logs)
- ✅ Upload application logs

**Trigger:**
- Push vào `main` hoặc `develop`
- Pull Request
- Manual trigger (workflow_dispatch)

**Xem kết quả:**
1. Vào repository trên GitHub
2. Click tab **Actions**
3. Chọn workflow run để xem chi tiết
4. Download artifacts để xem reports

**Chi tiết:** Xem file `docs/GITHUB_ACTIONS_CI_CD_SETUP.md`

### Jenkins Pipeline

Framework được tích hợp với **Jenkins** để tự động build, start app, và chạy tests.

**Jenkinsfile:** `Jenkinsfile`

**Pipeline Stages:**
1. **Checkout** - Checkout source code
2. **Build App** - Build Spring Boot application (skip tests)
3. **Start App** - Start Spring Boot application và chờ ready
4. **Run UI Tests** - Chạy automation tests
5. **Archive Reports** - Archive test reports và logs

**Cấu hình Jenkins:**
1. Tạo new Pipeline job
2. Chọn "Pipeline script from SCM"
3. Chọn Git repository
4. Script path: `Jenkinsfile`
5. Save và Build

**Xem kết quả:**
- Test reports: `test-output/`
- Surefire reports: `target/surefire-reports/`
- Application logs: `app.log`

---

## 🔍 Troubleshooting

### Lỗi: "WebDriver not found"

**Giải pháp:**
- Selenium 4.x tự động quản lý browser drivers
- Đảm bảo có kết nối internet khi chạy lần đầu
- Kiểm tra version browser đã cài đặt
- Đối với CI/CD, Chrome được cài đặt tự động trong workflow

### Lỗi: "Connection refused"

**Giải pháp:**
- Kiểm tra ứng dụng Spring Boot đang chạy tại `http://localhost:9090`
- Kiểm tra port trong `test.properties` và `application.properties`
- Kiểm tra firewall settings
- Đảm bảo database đã được setup và import schema

### Lỗi: "Element not found"

**Giải pháp:**
- Kiểm tra selectors trong `LoginOrRegisterPage.java`
- Đảm bảo HTML structure không thay đổi
- Tăng timeout trong `test.properties`
- Sử dụng explicit waits thay vì implicit waits

### Lỗi: "Screenshot not saved"

**Giải pháp:**
- Kiểm tra quyền ghi file trong thư mục `test-output/screenshots/`
- Đảm bảo đủ dung lượng ổ cứng
- Kiểm tra path trong `BaseTest.java`

### Test chạy chậm

**Giải pháp:**
- Giảm `implicit.wait` trong `test.properties`
- Sử dụng explicit waits thay vì implicit waits
- Chạy tests song song với TestNG parallel execution
- Sử dụng headless mode cho CI/CD

### Lỗi: "Database connection failed"

**Giải pháp:**
- Kiểm tra MySQL đang chạy
- Kiểm tra database credentials trong `application.properties`
- Đảm bảo database `vegana_store` đã được tạo
- Import schema từ `vegana.sql`

### Lỗi trên GitHub Actions

**Giải pháp:**
- Kiểm tra MySQL service đã ready
- Kiểm tra application đã start thành công
- Xem logs trong workflow run
- Download artifacts để xem chi tiết

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
    String screenshotPath = takeScreenshot(testName);
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

### 6. Headless Mode cho CI/CD

Tự động phát hiện môi trường CI/CD và chạy headless:

```java
private final boolean IS_GITHUB = 
    System.getenv("GITHUB_ACTIONS") != null;

if (IS_GITHUB) {
    co.addArguments("--headless=new");
    co.addArguments("--no-sandbox");
    co.addArguments("--disable-dev-shm-usage");
}
```

---

## 🤝 Đóng góp

Nếu bạn muốn đóng góp cho project:

1. Fork project
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

### Guidelines

- Follow code style và conventions
- Viết test cases cho tính năng mới
- Update documentation nếu cần
- Đảm bảo tất cả tests pass

---

## 📄 License

This project is licensed under the MIT License.

---

## 👨‍💻 Tác giả

Framework được tạo cho dự án **Vegana Shop Automation Testing**.

---

## 📚 Tài liệu tham khảo

- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [TestNG Documentation](https://testng.org/doc/documentation-main.html)
- [Extent Reports](https://www.extentreports.com/)
- [Log4j2 Documentation](https://logging.apache.org/log4j/2.x/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Documentation](https://maven.apache.org/guides/)

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
- ✅ **CI/CD Integration** - Jenkins và GitHub Actions
- ✅ **Headless Mode** - Tự động cho CI/CD

**Happy Testing! 🚀**

---

## 📞 Liên hệ

Nếu có câu hỏi hoặc vấn đề, vui lòng tạo issue trên GitHub repository.

---

**Last Updated:** 2024

