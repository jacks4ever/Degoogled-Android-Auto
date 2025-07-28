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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private BottomNavigationView bottomNav;
    private NissanDisplayAdapter displayAdapter;
    private ConnectionLogger logger;
    private ProtocolHandlerService protocolService;
    private boolean serviceBound = false;
    
    // Required permissions
    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize logging
        logger = ConnectionLogger.getInstance(this);
        logger.logInfo("Degoogled Android Auto started - MainActivity onCreate");
        logger.setVerboseLogging(true);
        
        // Initialize display adapter for screen optimization
        displayAdapter = new NissanDisplayAdapter(this);
        logger.logInfo("Nissan Pathfinder display adapter initialized: " + 
                displayAdapter.getDisplayProfile().toString());
        
        // Initialize bottom navigation
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            logger.logDebug("Navigation item selected: " + itemId);
            // Handle navigation item selection here
            return true;
        });
        
        // Check and request permissions
        checkAndRequestPermissions();
        
        // Start the protocol handler service
        startProtocolHandlerService();
        
        // Check if launched from USB connection
        handleUsbIntent(getIntent());
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
                logger.logInfo("USB accessory attached, starting connection process");
                // The enhanced ProtocolHandlerService will handle the connection automatically
                Toast.makeText(this, "Nissan Pathfinder detected - Connecting...", Toast.LENGTH_SHORT).show();
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                logger.logInfo("USB device attached, checking compatibility");
                Toast.makeText(this, "USB device detected - Checking compatibility...", Toast.LENGTH_SHORT).show();
            }
        }
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
        diagnosticInfo.append("App Version: 1.0.1\n");
        
        new AlertDialog.Builder(this)
                .setTitle("Nissan Pathfinder Diagnostics")
                .setMessage(diagnosticInfo.toString())
                .setPositiveButton("OK", null)
                .setNeutralButton("Export Logs", (dialog, which) -> exportLogs())
                .show();
    }
    
    /**
     * Check and request required permissions
     */
    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != 
                    PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                    permissionsToRequest.toArray(new String[0]), 
                    PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "Some permissions were denied. " +
                        "This may affect functionality.", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        logger.logInfo("MainActivity destroyed");
        // Logger will be closed by the service
    }
}