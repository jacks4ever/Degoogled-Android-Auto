package com.degoogled.androidauto.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Enhanced logging utility that logs to both Android's LogCat and an internal buffer
 * that can be exported to a file for troubleshooting vehicle compatibility issues.
 */
public class LogManager {
    private static final String TAG = "LogManager";
    private static final int MAX_LOG_ENTRIES = 10000;
    private static final ConcurrentLinkedQueue<String> logBuffer = new ConcurrentLinkedQueue<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    
    private static boolean verboseLogging = true;
    
    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR, VERBOSE
    }
    
    /**
     * Log a message with the specified log level
     */
    public static void log(String tag, String message, LogLevel level) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = timestamp + " | " + level.name() + " | " + tag + " | " + message;
        
        // Add to internal buffer
        addToBuffer(logEntry);
        
        // Also log to Android's LogCat
        switch (level) {
            case DEBUG:
                Log.d(tag, message);
                break;
            case INFO:
                Log.i(tag, message);
                break;
            case WARNING:
                Log.w(tag, message);
                break;
            case ERROR:
                Log.e(tag, message);
                break;
            case VERBOSE:
                if (verboseLogging) {
                    Log.v(tag, message);
                }
                break;
        }
    }
    
    /**
     * Log a debug message
     */
    public static void d(String tag, String message) {
        log(tag, message, LogLevel.DEBUG);
    }
    
    /**
     * Log an info message
     */
    public static void i(String tag, String message) {
        log(tag, message, LogLevel.INFO);
    }
    
    /**
     * Log a warning message
     */
    public static void w(String tag, String message) {
        log(tag, message, LogLevel.WARNING);
    }
    
    /**
     * Log a warning message with exception
     */
    public static void w(String tag, String message, Throwable throwable) {
        String fullMessage = message + " - " + throwable.getMessage();
        log(tag, fullMessage, LogLevel.WARNING);
        Log.w(tag, message, throwable); // Also log with stack trace to LogCat
    }
    
    /**
     * Log an error message
     */
    public static void e(String tag, String message) {
        log(tag, message, LogLevel.ERROR);
    }
    
    /**
     * Log an error message with exception
     */
    public static void e(String tag, String message, Throwable throwable) {
        String fullMessage = message + " - " + throwable.getMessage();
        log(tag, fullMessage, LogLevel.ERROR);
        Log.e(tag, message, throwable); // Also log with stack trace to LogCat
    }
    
    /**
     * Log a verbose message (only if verbose logging is enabled)
     */
    public static void v(String tag, String message) {
        log(tag, message, LogLevel.VERBOSE);
    }
    
    /**
     * Enable or disable verbose logging
     */
    public static void setVerboseLogging(boolean enabled) {
        verboseLogging = enabled;
        log(TAG, "Verbose logging " + (enabled ? "enabled" : "disabled"), LogLevel.INFO);
    }
    
    /**
     * Add a log entry to the internal buffer, maintaining the maximum size
     */
    private static synchronized void addToBuffer(String logEntry) {
        logBuffer.add(logEntry);
        while (logBuffer.size() > MAX_LOG_ENTRIES) {
            logBuffer.poll();
        }
    }
    
    /**
     * Get all logs from the internal buffer
     */
    public static List<String> getLogs() {
        return new ArrayList<>(logBuffer);
    }
    
    /**
     * Export logs to a file in the app's external files directory
     */
    public static File exportLogs(Context context) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "degoogled_android_auto_logs_" + timestamp + ".txt";
        
        File logsDir = new File(context.getExternalFilesDir(null), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        
        File logFile = new File(logsDir, fileName);
        
        try (FileOutputStream fos = new FileOutputStream(logFile)) {
            for (String logEntry : getLogs()) {
                fos.write((logEntry + "\n").getBytes());
            }
        }
        
        Log.i(TAG, "Logs exported to: " + logFile.getAbsolutePath());
        return logFile;
    }
    
    /**
     * Clear the log buffer
     */
    public static void clearLogs() {
        logBuffer.clear();
        Log.i(TAG, "Log buffer cleared");
    }
}