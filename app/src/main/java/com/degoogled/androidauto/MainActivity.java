package com.degoogled.androidauto;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.degoogled.androidauto.service.ProtocolHandlerService;
import com.degoogled.androidauto.logging.ConnectionLogger;
import com.degoogled.androidauto.ui.NissanDisplayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    // UI Components
    private TextView connectionStatus;
    private TextView deviceInfo;
    private TextView permissionStatus;
    private Button btnDiagnostics;
    private Button btnSettings;
    
    // Core components
    private NissanDisplayAdapter displayAdapter;
    private ConnectionLogger logger;
    private ProtocolHandlerService protocolService;
    private boolean serviceBound = false;
    
    // Required permissions for Android Auto functionality
    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    
    // Optional permissions that enhance functionality but aren't critical
    private static final String[] OPTIONAL_PERMISSIONS = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_CONTACTS
    };
    
    // Android 12+ specific permissions (required for Bluetooth functionality)
    private static final String[] ANDROID_12_PERMISSIONS = {
            "android.permission.BLUETOOTH_CONNECT"
    };
    
    // Android 14 specific permissions
    private static final String[] ANDROID_14_PERMISSIONS = {
            "android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE",
            "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize logging
        logger = ConnectionLogger.getInstance(this);
        logger.logInfo("Degoogled Android Auto v1.2.0 started - Android 14 compatible");
        logger.setVerboseLogging(true);
        
        // Initialize display adapter for Nissan Pathfinder optimization
        displayAdapter = new NissanDisplayAdapter(this);
        logger.logInfo("Nissan Pathfinder display adapter initialized: " + 
                displayAdapter.getDisplayProfile().toString());
        
        // Initialize UI components
        initializeUI();
        
        // Update initial status
        updateConnectionStatus("Initializing...", "#ffa500");
        updatePermissionStatus("Checking permissions...", "#ffa500");
        
        // Check and request permissions
        checkAndRequestPermissions();
        
        // Check if launched from USB connection
        handleUsbIntent(getIntent());
    }
    
    /**
     * Initialize UI components and set up event handlers
     */
    private void initializeUI() {
        connectionStatus = findViewById(R.id.connectionStatus);
        deviceInfo = findViewById(R.id.deviceInfo);
        permissionStatus = findViewById(R.id.permissionStatus);
        btnDiagnostics = findViewById(R.id.btnDiagnostics);
        btnSettings = findViewById(R.id.btnSettings);
        
        // Set up button click handlers
        btnDiagnostics.setOnClickListener(v -> showDiagnosticInfo());
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        
        logger.logInfo("UI components initialized");
    }
    
    /**
     * Update connection status display
     */
    private void updateConnectionStatus(String status, String color) {
        runOnUiThread(() -> {
            if (connectionStatus != null) {
                connectionStatus.setText(status);
                connectionStatus.setTextColor(android.graphics.Color.parseColor(color));
            }
        });
    }
    
    /**
     * Update permission status display
     */
    private void updatePermissionStatus(String status, String color) {
        runOnUiThread(() -> {
            if (permissionStatus != null) {
                permissionStatus.setText(status);
                permissionStatus.setTextColor(android.graphics.Color.parseColor(color));
            }
        });
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleUsbIntent(intent);
    }
    
    /**
     * Handle USB intent that may have launched the app
     */
    private void handleUsbIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            logger.logInfo("Received intent action: " + action);
            
            if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                logger.logInfo("USB accessory attached - Nissan Pathfinder detected");
                updateConnectionStatus("Nissan Pathfinder detected - Connecting...", "#4CAF50");
                updateDeviceInfo("2023 Nissan Pathfinder connected via USB");
                
                // Start the protocol handler service
                startProtocolHandlerService();
                
                Toast.makeText(this, "Connecting to Nissan Pathfinder...", Toast.LENGTH_SHORT).show();
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                logger.logInfo("USB device attached, checking compatibility");
                updateConnectionStatus("USB device detected - Checking compatibility...", "#ffa500");
                Toast.makeText(this, "USB device detected - Checking compatibility...", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Update device info display
     */
    private void updateDeviceInfo(String info) {
        runOnUiThread(() -> {
            if (deviceInfo != null) {
                deviceInfo.setText(info);
            }
        });
    }
    
    /**
     * Start the protocol handler service
     */
    private void startProtocolHandlerService() {
        Intent serviceIntent = new Intent(this, ProtocolHandlerService.class);
        startService(serviceIntent);
        logger.logInfo("ProtocolHandlerService started");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_export_logs) {
            exportLogs();
            return true;
        } else if (id == R.id.action_diagnostics) {
            showDiagnosticInfo();
            return true;
        // Removed verbose logging menu item for now
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void exportLogs() {
        try {
            java.io.File logDir = logger.exportLogs();
            if (logDir != null) {
                Toast.makeText(this, "Logs exported to: " + logDir.getAbsolutePath(), 
                             Toast.LENGTH_LONG).show();
                logger.logInfo("Logs exported successfully");
            } else {
                Toast.makeText(this, "Failed to export logs", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error exporting logs: " + e.getMessage(), 
                         Toast.LENGTH_LONG).show();
            logger.logError("Failed to export logs: " + e.getMessage());
        }
    }
    
    private void toggleVerboseLogging() {
        // Toggle verbose logging
        boolean currentState = true; // We'd need to track this state
        logger.setVerboseLogging(!currentState);
        Toast.makeText(this, "Verbose logging " + (!currentState ? "enabled" : "disabled"), 
                     Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Show diagnostic information dialog
     */
    private void showDiagnosticInfo() {
        StringBuilder diagnosticInfo = new StringBuilder();
        
        // Display information
        NissanDisplayAdapter.DisplayProfile profile = displayAdapter.getDisplayProfile();
        diagnosticInfo.append("=== Nissan Pathfinder Compatibility ===\n");
        diagnosticInfo.append("Display: ").append(profile.widthPx).append("x").append(profile.heightPx).append("\n");
        diagnosticInfo.append("Size: ").append(profile.size).append("\n");
        diagnosticInfo.append("Density: ").append(profile.density).append(" (").append(profile.densityDpi).append(" dpi)\n");
        diagnosticInfo.append("Font Scale: ").append(profile.fontScale).append("\n");
        diagnosticInfo.append("Touch Target: ").append(profile.optimalTouchTargetPx).append("px\n");
        diagnosticInfo.append("Orientation: ").append(profile.orientation).append("\n\n");
        
        // Layout recommendations
        NissanDisplayAdapter.LayoutRecommendations layout = displayAdapter.getLayoutRecommendations();
        diagnosticInfo.append("=== Layout Recommendations ===\n");
        diagnosticInfo.append("Max Columns: ").append(layout.maxColumns).append("\n");
        diagnosticInfo.append("Max Rows: ").append(layout.maxRows).append("\n");
        diagnosticInfo.append("Margins: ").append(layout.marginDp).append("dp\n");
        diagnosticInfo.append("Padding: ").append(layout.paddingDp).append("dp\n\n");
        
        // System information
        diagnosticInfo.append("=== System Information ===\n");
        diagnosticInfo.append("Device: ").append(android.os.Build.MODEL).append("\n");
        diagnosticInfo.append("Android: ").append(android.os.Build.VERSION.RELEASE).append("\n");
        diagnosticInfo.append("App Version: 1.2.0\n");
        
        new AlertDialog.Builder(this)
                .setTitle("Nissan Pathfinder Diagnostics")
                .setMessage(diagnosticInfo.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("Export Logs", (dialog, which) -> exportLogs())
                .show();
    }
    
    /**
     * Check and request required permissions for Android 14
     */
    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        logger.logInfo("Checking permissions for Android " + android.os.Build.VERSION.RELEASE + 
                      " (API " + android.os.Build.VERSION.SDK_INT + ")");
        
        // Check required permissions
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != 
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
                logger.logInfo("Required permission needed: " + permission);
            }
        }
        
        // Check Android 12+ specific permissions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            for (String permission : ANDROID_12_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != 
                        PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                    logger.logInfo("Android 12+ permission needed: " + permission);
                }
            }
        }
        
        // Check Android 14 specific permissions (these are usually granted automatically)
        if (android.os.Build.VERSION.SDK_INT >= 34) { // Android 14
            for (String permission : ANDROID_14_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != 
                        PackageManager.PERMISSION_GRANTED) {
                    logger.logInfo("Android 14 permission status: " + permission + " - checking");
                    // Note: These are usually auto-granted for foreground services
                }
            }
        }
        
        // Check optional permissions (don't block functionality if denied)
        for (String permission : OPTIONAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != 
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
                logger.logInfo("Optional permission needed: " + permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            logger.logInfo("Requesting " + permissionsToRequest.size() + " permissions");
            updatePermissionStatus("Requesting " + permissionsToRequest.size() + " permissions...", "#ffa500");
            
            ActivityCompat.requestPermissions(this, 
                    permissionsToRequest.toArray(new String[0]), 
                    PERMISSION_REQUEST_CODE);
        } else {
            logger.logInfo("All permissions already granted");
            updatePermissionStatus("All permissions granted ✓", "#4CAF50");
            onPermissionsReady();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allRequiredGranted = true;
            List<String> deniedRequired = new ArrayList<>();
            List<String> deniedOptional = new ArrayList<>();
            
            // Check which permissions were denied
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    String permission = permissions[i];
                    boolean isRequired = false;
                    
                    // Check if it's a required permission
                    for (String reqPerm : REQUIRED_PERMISSIONS) {
                        if (reqPerm.equals(permission)) {
                            deniedRequired.add(permission);
                            allRequiredGranted = false;
                            isRequired = true;
                            break;
                        }
                    }
                    
                    // Check if it's an Android 12+ required permission
                    if (!isRequired && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        for (String reqPerm : ANDROID_12_PERMISSIONS) {
                            if (reqPerm.equals(permission)) {
                                deniedRequired.add(permission);
                                allRequiredGranted = false;
                                isRequired = true;
                                break;
                            }
                        }
                    }
                    
                    // If not required, it's optional
                    if (!isRequired) {
                        deniedOptional.add(permission);
                    }
                }
            }
            
            // Log permission results
            logger.logInfo("Permission results - Required granted: " + allRequiredGranted + 
                          ", Denied required: " + deniedRequired.size() + 
                          ", Denied optional: " + deniedOptional.size());
            
            if (!allRequiredGranted) {
                String message = "Critical permissions denied - may affect Nissan Pathfinder connection";
                updatePermissionStatus(message, "#f44336");
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                logger.logWarning("Critical permissions denied: " + deniedRequired.toString());
                
                // Show explanation dialog
                showPermissionExplanationDialog(deniedRequired);
            } else if (!deniedOptional.isEmpty()) {
                String message = "Some optional permissions denied - limited features";
                updatePermissionStatus(message, "#ff9800");
                Toast.makeText(this, "Some features may be limited", Toast.LENGTH_SHORT).show();
                logger.logInfo("Optional permissions denied: " + deniedOptional.toString());
                onPermissionsReady();
            } else {
                logger.logInfo("All permissions granted successfully");
                updatePermissionStatus("All permissions granted ✓", "#4CAF50");
                onPermissionsReady();
            }
        }
    }
    
    /**
     * Called when permissions are ready (either all granted or handled)
     */
    private void onPermissionsReady() {
        logger.logInfo("Permissions ready - initializing Nissan Pathfinder connection");
        
        // Update UI to show ready state
        updateConnectionStatus("Ready for USB connection", "#4CAF50");
        updatePermissionStatus("Permissions configured ✓", "#4CAF50");
        
        Toast.makeText(this, "Ready to connect to Nissan Pathfinder", Toast.LENGTH_SHORT).show();
        
        // Start services if not already started
        startProtocolHandlerService();
    }
    
    /**
     * Show settings dialog
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        
        String[] options = {
            "Open App Permissions",
            "View Connection Logs", 
            "Reset Connection",
            "About"
        };
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Open App Permissions
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    break;
                case 1: // View Connection Logs
                    exportLogs();
                    break;
                case 2: // Reset Connection
                    resetConnection();
                    break;
                case 3: // About
                    showAboutDialog();
                    break;
            }
        });
        
        builder.show();
    }
    
    /**
     * Reset connection
     */
    private void resetConnection() {
        logger.logInfo("Resetting connection...");
        updateConnectionStatus("Resetting connection...", "#ffa500");
        
        // Restart the protocol handler service
        Intent serviceIntent = new Intent(this, ProtocolHandlerService.class);
        stopService(serviceIntent);
        
        // Wait a moment then restart
        new android.os.Handler().postDelayed(() -> {
            startProtocolHandlerService();
            updateConnectionStatus("Ready for USB connection", "#4CAF50");
            Toast.makeText(this, "Connection reset complete", Toast.LENGTH_SHORT).show();
        }, 1000);
    }
    
    /**
     * Show about dialog
     */
    private void showAboutDialog() {
        String aboutText = "Degoogled Android Auto v1.2.0\n\n" +
                          "A privacy-focused Android Auto implementation\n" +
                          "specifically optimized for 2023 Nissan Pathfinder.\n\n" +
                          "Features:\n" +
                          "• No Google Services required\n" +
                          "• VLC media player integration\n" +
                          "• Enhanced USB communication\n" +
                          "• Nissan-specific optimizations\n\n" +
                          "The interface appears on your car's display,\n" +
                          "not on this phone screen.";
        
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage(aboutText)
                .setPositiveButton("OK", null)
                .show();
    }
    
    /**
     * Show explanation dialog for denied critical permissions
     */
    private void showPermissionExplanationDialog(List<String> deniedPermissions) {
        StringBuilder message = new StringBuilder();
        message.append("The following permissions are required for Nissan Pathfinder compatibility:\n\n");
        
        for (String permission : deniedPermissions) {
            switch (permission) {
                case android.Manifest.permission.ACCESS_FINE_LOCATION:
                case android.Manifest.permission.ACCESS_COARSE_LOCATION:
                    message.append("• Location: Required for navigation and Android Auto protocol\n");
                    break;
                case android.Manifest.permission.RECORD_AUDIO:
                    message.append("• Microphone: Required for voice commands and calls\n");
                    break;
                case android.Manifest.permission.READ_PHONE_STATE:
                    message.append("• Phone: Required for call handling and device identification\n");
                    break;
                case "android.permission.BLUETOOTH_CONNECT":
                    message.append("• Bluetooth Connect: Required for wireless connectivity\n");
                    break;
                case "android.permission.BLUETOOTH_SCAN":
                    message.append("• Bluetooth Scan: Required for device discovery\n");
                    break;
            }
        }
        
        message.append("\nPlease grant these permissions in Settings > Apps > Degoogled Android Auto > Permissions");
        
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage(message.toString())
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Continue Anyway", (dialog, which) -> onPermissionsReady())
                .setCancelable(false)
                .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        logger.logInfo("MainActivity destroyed");
        // Logger will be closed by the service
    }
}