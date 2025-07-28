package com.degoogled.androidauto.logging;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Comprehensive logging system for Android Auto connection analysis
 * Specifically designed for Nissan Pathfinder compatibility debugging
 */
public class ConnectionLogger {
    private static final String TAG = "DegoogledAA_Logger";
    private static final String LOG_FILE_PREFIX = "degoogled_aa_";
    private static final String LOG_FILE_EXTENSION = ".log";
    
    private static ConnectionLogger instance;
    private Context context;
    private FileWriter logWriter;
    private SimpleDateFormat dateFormat;
    private boolean verboseLogging = true;
    
    // Connection phase tracking
    public enum ConnectionPhase {
        USB_DETECTION,
        DEVICE_ENUMERATION,
        ACCESSORY_HANDSHAKE,
        PROTOCOL_NEGOTIATION,
        AUTHENTICATION,
        SERVICE_BINDING,
        APP_PROJECTION,
        READY
    }
    
    // Nissan-specific tracking
    private HashMap<String, Object> nissanMetrics = new HashMap<>();
    
    private ConnectionLogger(Context context) {
        this.context = context.getApplicationContext();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        initializeLogFile();
    }
    
    public static synchronized ConnectionLogger getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectionLogger(context);
        }
        return instance;
    }
    
    private void initializeLogFile() {
        try {
            File logDir = new File(context.getExternalFilesDir(null), "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File logFile = new File(logDir, LOG_FILE_PREFIX + timestamp + LOG_FILE_EXTENSION);
            
            logWriter = new FileWriter(logFile, true);
            logConnectionStart();
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize log file", e);
        }
    }
    
    private void logConnectionStart() {
        logInfo("=== Degoogled Android Auto Connection Session Started ===");
        logInfo("Device: " + android.os.Build.MODEL);
        logInfo("Android Version: " + android.os.Build.VERSION.RELEASE);
        logInfo("Target Vehicle: 2023 Nissan Pathfinder");
        logInfo("========================================================");
    }
    
    public void logConnectionPhase(ConnectionPhase phase, String details) {
        String message = String.format("[%s] %s: %s", phase.name(), getCurrentTimestamp(), details);
        logToFile(message);
        
        if (verboseLogging) {
            Log.i(TAG, message);
        }
    }
    
    public void logUSBDeviceDetection(UsbDevice device) {
        if (device == null) {
            logError("USB device detection failed - null device");
            return;
        }
        
        StringBuilder deviceInfo = new StringBuilder();
        deviceInfo.append("USB Device Detected:\n");
        deviceInfo.append("  Vendor ID: 0x").append(Integer.toHexString(device.getVendorId())).append(" (").append(device.getVendorId()).append(")\n");
        deviceInfo.append("  Product ID: 0x").append(Integer.toHexString(device.getProductId())).append(" (").append(device.getProductId()).append(")\n");
        deviceInfo.append("  Device Name: ").append(device.getDeviceName()).append("\n");
        deviceInfo.append("  Manufacturer: ").append(device.getManufacturerName()).append("\n");
        deviceInfo.append("  Product: ").append(device.getProductName()).append("\n");
        deviceInfo.append("  Serial: ").append(device.getSerialNumber()).append("\n");
        deviceInfo.append("  Class: ").append(device.getDeviceClass()).append("\n");
        deviceInfo.append("  Subclass: ").append(device.getDeviceSubclass()).append("\n");
        deviceInfo.append("  Protocol: ").append(device.getDeviceProtocol()).append("\n");
        deviceInfo.append("  Interface Count: ").append(device.getInterfaceCount());
        
        logConnectionPhase(ConnectionPhase.USB_DETECTION, deviceInfo.toString());
        
        // Check if this matches known Nissan patterns
        checkNissanCompatibility(device);
    }
    
    private void checkNissanCompatibility(UsbDevice device) {
        boolean isNissanCompatible = false;
        String compatibilityNotes = "";
        
        // Check for standard Android Auto AOAP IDs
        if (device.getVendorId() == 0x18D1) {
            if (device.getProductId() >= 0x2D00 && device.getProductId() <= 0x2D05) {
                isNissanCompatible = true;
                compatibilityNotes = "Standard AOAP device - should work with Nissan Pathfinder";
            }
        }
        
        // Check manufacturer name for Nissan-specific strings
        String manufacturer = device.getManufacturerName();
        if (manufacturer != null) {
            if (manufacturer.toLowerCase().contains("nissan") || 
                manufacturer.toLowerCase().contains("android")) {
                isNissanCompatible = true;
                compatibilityNotes += " Nissan-compatible manufacturer detected";
            }
        }
        
        nissanMetrics.put("compatible", isNissanCompatible);
        nissanMetrics.put("compatibility_notes", compatibilityNotes);
        
        logInfo("Nissan Pathfinder Compatibility: " + (isNissanCompatible ? "LIKELY COMPATIBLE" : "UNKNOWN") + 
                (compatibilityNotes.isEmpty() ? "" : " - " + compatibilityNotes));
    }
    
    public void logHandshakeAttempt(String protocol, String version, boolean success) {
        String result = success ? "SUCCESS" : "FAILED";
        String message = String.format("Handshake attempt - Protocol: %s, Version: %s, Result: %s", 
                                      protocol, version, result);
        logConnectionPhase(ConnectionPhase.ACCESSORY_HANDSHAKE, message);
        
        if (!success) {
            logError("Handshake failed - may need fallback authentication for Nissan Pathfinder");
        }
    }
    
    public void logAuthenticationStep(String step, String details, boolean success) {
        String result = success ? "SUCCESS" : "FAILED";
        String message = String.format("Auth Step [%s]: %s - %s", step, details, result);
        logConnectionPhase(ConnectionPhase.AUTHENTICATION, message);
        
        // Track authentication patterns for Nissan compatibility
        if (step.toLowerCase().contains("nissan") || step.toLowerCase().contains("pathfinder")) {
            nissanMetrics.put("nissan_auth_" + step, success);
        }
    }
    
    public void logDisplayCharacteristics(int width, int height, float density, String orientation) {
        String displayInfo = String.format("Display: %dx%d, density=%.2f, orientation=%s", 
                                          width, height, density, orientation);
        logInfo("Nissan Pathfinder " + displayInfo);
        
        // Store for UI scaling decisions
        nissanMetrics.put("display_width", width);
        nissanMetrics.put("display_height", height);
        nissanMetrics.put("display_density", density);
        nissanMetrics.put("display_orientation", orientation);
    }
    
    public void logTouchCalibration(float touchAccuracy, int targetSize, boolean calibrationSuccess) {
        String touchInfo = String.format("Touch calibration - Accuracy: %.2f, Target size: %dpx, Success: %s",
                                        touchAccuracy, targetSize, calibrationSuccess);
        logInfo("Nissan Pathfinder " + touchInfo);
        
        nissanMetrics.put("touch_accuracy", touchAccuracy);
        nissanMetrics.put("optimal_target_size", targetSize);
    }
    
    public void logProtocolMessage(String direction, String messageType, byte[] data, boolean success) {
        String message = String.format("Protocol [%s] %s: %s (%d bytes) - %s",
                                      direction, messageType, 
                                      bytesToHex(data, Math.min(data.length, 32)), // First 32 bytes
                                      data.length,
                                      success ? "OK" : "ERROR");
        logConnectionPhase(ConnectionPhase.PROTOCOL_NEGOTIATION, message);
    }
    
    public void logServiceBinding(String serviceName, boolean success, String errorDetails) {
        String message = String.format("Service binding [%s]: %s%s",
                                      serviceName, 
                                      success ? "SUCCESS" : "FAILED",
                                      errorDetails != null ? " - " + errorDetails : "");
        logConnectionPhase(ConnectionPhase.SERVICE_BINDING, message);
    }
    
    public void logAppProjection(String appName, String action, boolean success) {
        String message = String.format("App projection [%s] %s: %s", 
                                      appName, action, success ? "SUCCESS" : "FAILED");
        logConnectionPhase(ConnectionPhase.APP_PROJECTION, message);
    }
    
    public void logError(String message) {
        String logMessage = "[ERROR] " + getCurrentTimestamp() + ": " + message;
        logToFile(logMessage);
        Log.e(TAG, logMessage);
    }
    
    public void logError(String message, Exception e) {
        String logMessage = "[ERROR] " + getCurrentTimestamp() + ": " + message + " - " + e.getMessage();
        logToFile(logMessage);
        Log.e(TAG, logMessage, e);
    }
    
    public void logConnection(String message) {
        String logMessage = "[CONNECTION] " + getCurrentTimestamp() + ": " + message;
        logToFile(logMessage);
        if (verboseLogging) {
            Log.i(TAG, logMessage);
        }
    }
    
    public void logRawData(String direction, byte[] data) {
        if (data != null && verboseLogging) {
            String message = String.format("Raw data [%s]: %s (%d bytes)", 
                                          direction, bytesToHex(data, 32), data.length);
            logDebug(message);
        }
    }
    
    public void logMessage(String direction, com.degoogled.androidauto.protocol.messages.Message message) {
        if (message != null) {
            String logMessage = String.format("Message [%s]: %s", direction, message.toString());
            logDebug(logMessage);
        }
    }
    
    public void logWarning(String message) {
        String logMessage = "[WARNING] " + getCurrentTimestamp() + ": " + message;
        logToFile(logMessage);
        Log.w(TAG, logMessage);
    }
    
    public void logInfo(String message) {
        String logMessage = "[INFO] " + getCurrentTimestamp() + ": " + message;
        logToFile(logMessage);
        if (verboseLogging) {
            Log.i(TAG, logMessage);
        }
    }
    
    public void logDebug(String message) {
        if (verboseLogging) {
            String logMessage = "[DEBUG] " + getCurrentTimestamp() + ": " + message;
            logToFile(logMessage);
            Log.d(TAG, logMessage);
        }
    }
    
    private void logToFile(String message) {
        if (logWriter != null) {
            try {
                logWriter.write(message + "\n");
                logWriter.flush();
            } catch (IOException e) {
                Log.e(TAG, "Failed to write to log file", e);
            }
        }
    }
    
    private String getCurrentTimestamp() {
        return dateFormat.format(new Date());
    }
    
    private String bytesToHex(byte[] bytes, int maxLength) {
        if (bytes == null) return "null";
        
        StringBuilder hex = new StringBuilder();
        int length = Math.min(bytes.length, maxLength);
        
        for (int i = 0; i < length; i++) {
            hex.append(String.format("%02X ", bytes[i]));
        }
        
        if (bytes.length > maxLength) {
            hex.append("...");
        }
        
        return hex.toString().trim();
    }
    
    public File exportLogs() {
        try {
            if (logWriter != null) {
                logWriter.flush();
            }
            
            // Create export summary
            logInfo("=== Connection Session Summary ===");
            logInfo("Nissan Pathfinder Compatibility Metrics:");
            for (String key : nissanMetrics.keySet()) {
                logInfo("  " + key + ": " + nissanMetrics.get(key));
            }
            logInfo("=== End Session ===");
            
            File logDir = new File(context.getExternalFilesDir(null), "logs");
            return logDir; // Return directory for user to access all logs
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to export logs", e);
            return null;
        }
    }
    
    public void setVerboseLogging(boolean verbose) {
        this.verboseLogging = verbose;
        logInfo("Verbose logging " + (verbose ? "enabled" : "disabled"));
    }
    
    public void close() {
        if (logWriter != null) {
            try {
                logInfo("=== Degoogled Android Auto Connection Session Ended ===");
                logWriter.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close log file", e);
            }
        }
    }
}