package com.java.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for logging
 */
public class LoggerUtil {
    private static Logger logger = LogManager.getLogger(LoggerUtil.class);

    /**
     * Get logger instance
     * @return Logger instance
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Get logger for specific class
     * @param clazz Class to get logger for
     * @return Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    /**
     * Log info message
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Log error message
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * Log warning message
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Log debug message
     */
    public static void debug(String message) {
        logger.debug(message);
    }
}

