package com.java.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Manager class for Extent Reports
 * Đã tối ưu hóa để tự động tạo thư mục báo cáo.
 */
public class ExtentReportManager {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    /**
     * Initialize Extent Reports
     */
    public static ExtentReports getInstance() {
        if (extent == null) {
            // Tạo tên file theo thời gian
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "ExtentReport_" + timestamp + ".html";

            // Đường dẫn thư mục báo cáo
            String projectPath = System.getProperty("user.dir");
            String reportFolderPath = projectPath + File.separator + "test-output" + File.separator + "reports";
            String reportPath = reportFolderPath + File.separator + fileName;

            // --- QUAN TRỌNG: Tạo thư mục nếu chưa tồn tại ---
            File reportDir = new File(reportFolderPath);
            if (!reportDir.exists()) {
                reportDir.mkdirs();
                System.out.println("📁 Đã tạo thư mục báo cáo: " + reportFolderPath);
            }

            ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(reportPath);

            // Cấu hình giao diện báo cáo
            htmlReporter.config().setTheme(Theme.STANDARD); // Hoặc Theme.DARK nếu thích màu tối
            htmlReporter.config().setDocumentTitle("Vegana Shop Automation Report");
            htmlReporter.config().setReportName("Kết quả kiểm thử tự động (Selenium + TestNG)");
            htmlReporter.config().setEncoding("utf-8");

            // Thêm CSS tùy chỉnh để ảnh chụp màn hình Full Page hiển thị đẹp hơn
            htmlReporter.config().setCSS(".r-img { width: 50%; }"); // Giảm kích thước ảnh hiển thị ban đầu

            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);

            // Thông tin hệ thống
            extent.setSystemInfo("Project", "Vegana Shop");
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("User Name", System.getProperty("user.name"));
        }
        return extent;
    }

    /**
     * Create test in report
     */
    public static ExtentTest createTest(String testName, String description) {
        ExtentTest extentTest = getInstance().createTest(testName, description);
        test.set(extentTest);
        return extentTest;
    }

    /**
     * Get current test
     */
    public static ExtentTest getTest() {
        return test.get();
    }

    /**
     * Flush report
     */
    public static void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}