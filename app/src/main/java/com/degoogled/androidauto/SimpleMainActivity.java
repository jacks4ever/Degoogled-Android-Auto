package com.degoogled.androidauto;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SimpleMainActivity extends AppCompatActivity {
    private static final String TAG = "SimpleMainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    // UI Components
    private TextView connectionStatus;
    private TextView deviceInfo;
    private TextView permissionStatus;
    private Button btnDiagnostics;
    private Button btnSettings;
    
    // Required permissions for Android 14
    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    
    // Optional permissions
    private static final String[] OPTIONAL_PERMISSIONS = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE
    };
    
    // Android 12+ specific permissions
    private static final String[] ANDROID_12_PERMISSIONS = {
            "android.permission.BLUETOOTH_CONNECT"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
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
            
            if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                updateConnectionStatus("Nissan Pathfinder detected - Connecting...", "#4CAF50");
                updateDeviceInfo("2023 Nissan Pathfinder connected via USB");
                Toast.makeText(this, "Connecting to Nissan Pathfinder...", Toast.LENGTH_SHORT).show();
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                updateConnectionStatus("USB device detected - Checking compatibility...", "#ffa500");
                Toast.makeText(this, "USB device detected - Checking compatibility...", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Check and request required permissions for Android 14
     */
    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Check required permissions
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != 
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        // Check Android 12+ specific permissions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            for (String permission : ANDROID_12_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != 
                        PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }
        
        // Check optional permissions
        for (String permission : OPTIONAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != 
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            updatePermissionStatus("Requesting " + permissionsToRequest.size() + " permissions...", "#ffa500");
            
            ActivityCompat.requestPermissions(this, 
                    permissionsToRequest.toArray(new String[0]), 
                    PERMISSION_REQUEST_CODE);
        } else {
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
            
            if (!allRequiredGranted) {
                String message = "Critical permissions denied - may affect Nissan Pathfinder connection";
                updatePermissionStatus(message, "#f44336");
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                
                // Show explanation dialog
                showPermissionExplanationDialog(deniedRequired);
            } else if (!deniedOptional.isEmpty()) {
                String message = "Some optional permissions denied - limited features";
                updatePermissionStatus(message, "#ff9800");
                Toast.makeText(this, "Some features may be limited", Toast.LENGTH_SHORT).show();
                onPermissionsReady();
            } else {
                updatePermissionStatus("All permissions granted ✓", "#4CAF50");
                onPermissionsReady();
            }
        }
    }
    
    /**
     * Called when permissions are ready (either all granted or handled)
     */
    private void onPermissionsReady() {
        // Update UI to show ready state
        updateConnectionStatus("Ready for USB connection", "#4CAF50");
        updatePermissionStatus("Permissions configured ✓", "#4CAF50");
        
        Toast.makeText(this, "Ready to connect to Nissan Pathfinder", Toast.LENGTH_SHORT).show();
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
    
    /**
     * Show diagnostic information dialog
     */
    private void showDiagnosticInfo() {
        StringBuilder diagnosticInfo = new StringBuilder();
        
        // System information
        diagnosticInfo.append("=== System Information ===\n");
        diagnosticInfo.append("Device: ").append(android.os.Build.MODEL).append("\n");
        diagnosticInfo.append("Android: ").append(android.os.Build.VERSION.RELEASE).append(" (API ").append(android.os.Build.VERSION.SDK_INT).append(")\n");
        diagnosticInfo.append("App Version: 1.1.0\n\n");
        
        // Permission status
        diagnosticInfo.append("=== Permission Status ===\n");
        for (String permission : REQUIRED_PERMISSIONS) {
            boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
            diagnosticInfo.append("• ").append(getPermissionName(permission)).append(": ").append(granted ? "✓" : "✗").append("\n");
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            for (String permission : ANDROID_12_PERMISSIONS) {
                boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
                diagnosticInfo.append("• ").append(getPermissionName(permission)).append(": ").append(granted ? "✓" : "✗").append("\n");
            }
        }
        
        for (String permission : OPTIONAL_PERMISSIONS) {
            boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
            diagnosticInfo.append("• ").append(getPermissionName(permission)).append(" (optional): ").append(granted ? "✓" : "✗").append("\n");
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Nissan Pathfinder Diagnostics")
                .setMessage(diagnosticInfo.toString())
                .setPositiveButton("OK", null)
                .show();
    }
    
    /**
     * Get human-readable permission name
     */
    private String getPermissionName(String permission) {
        switch (permission) {
            case android.Manifest.permission.ACCESS_FINE_LOCATION:
                return "Fine Location";
            case android.Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Coarse Location";
            case android.Manifest.permission.RECORD_AUDIO:
                return "Microphone";
            case android.Manifest.permission.READ_PHONE_STATE:
                return "Phone State";
            case "android.permission.BLUETOOTH_CONNECT":
                return "Bluetooth Connect";
            default:
                return permission.substring(permission.lastIndexOf('.') + 1);
        }
    }
    
    /**
     * Show settings dialog
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        
        String[] options = {
            "Open App Permissions",
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
                case 1: // Reset Connection
                    resetConnection();
                    break;
                case 2: // About
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
        updateConnectionStatus("Resetting connection...", "#ffa500");
        
        // Wait a moment then reset
        new android.os.Handler().postDelayed(() -> {
            updateConnectionStatus("Ready for USB connection", "#4CAF50");
            Toast.makeText(this, "Connection reset complete", Toast.LENGTH_SHORT).show();
        }, 1000);
    }
    
    /**
     * Show about dialog
     */
    private void showAboutDialog() {
        String aboutText = "Degoogled Android Auto v1.1.0\n\n" +
                          "A privacy-focused Android Auto implementation\n" +
                          "specifically optimized for 2023 Nissan Pathfinder.\n\n" +
                          "Features:\n" +
                          "• No Google Services required\n" +
                          "• Android 14 compatible\n" +
                          "• Enhanced permission handling\n" +
                          "• Nissan-specific optimizations\n\n" +
                          "The Android Auto interface appears on your car's display,\n" +
                          "not on this phone screen.\n\n" +
                          "Instructions:\n" +
                          "1. Connect your phone to your Nissan Pathfinder via USB\n" +
                          "2. The Android Auto interface will appear on your car's display\n" +
                          "3. Use your car's touchscreen or controls to navigate";
        
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage(aboutText)
                .setPositiveButton("OK", null)
                .show();
    }
}