package com.java.automation.selenium;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotOnFailureExtension implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        // Lấy instance test hiện tại (đã kế thừa BaseSeleniumTest)
        Object testInstance = context.getRequiredTestInstance();

        if (testInstance instanceof BaseSeleniumTest &&
                BaseSeleniumTest.driver instanceof TakesScreenshot ts) {

            try {
                File src = ts.getScreenshotAs(OutputType.FILE);

                String methodName = context.getDisplayName();
                String time = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

                Path dest = Path.of("screenshots",
                        methodName + "_" + time + ".png");

                Files.createDirectories(dest.getParent());
                Files.copy(src.toPath(), dest);

                System.out.println("Saved screenshot: " + dest.toAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
