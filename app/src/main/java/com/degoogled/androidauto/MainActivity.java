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
import com.degoogled.androidauto.ui.DisplayAdapter;
import com.degoogled.androidauto.utils.LogManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProtocolHandlerService.ProtocolStateListener {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private BottomNavigationView bottomNav;
    private DisplayAdapter displayAdapter;
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
        LogManager.i(TAG, "Degoogled Android Auto started");
        LogManager.setVerboseLogging(true);
        
        // Initialize display adapter for screen optimization
        displayAdapter = new DisplayAdapter(this);
        LogManager.i(TAG, "Display adapter initialized. Is Nissan Pathfinder: " + 
                displayAdapter.isNissanPathfinder());
        
        // Initialize bottom navigation
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            LogManager.d(TAG, "Navigation item selected: " + itemId);
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
            LogManager.i(TAG, "Received intent action: " + action);
            
            if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                LogManager.i(TAG, "USB accessory attached, connecting to head unit");
                if (protocolService != null) {
                    protocolService.connectToHeadUnit();
                }
            }
        }
    }
    
    /**
     * Start the protocol handler service
     */
    private void startProtocolHandlerService() {
        Intent serviceIntent = new Intent(this, ProtocolHandlerService.class);
        startForegroundService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    /**
     * Service connection for binding to the protocol handler service
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ProtocolHandlerService.LocalBinder binder = (ProtocolHandlerService.LocalBinder) service;
            protocolService = binder.getService();
            protocolService.setStateListener(MainActivity.this);
            serviceBound = true;
            
            LogManager.i(TAG, "Connected to ProtocolHandlerService");
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            LogManager.i(TAG, "Disconnected from ProtocolHandlerService");
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_connect) {
            if (protocolService != null) {
                protocolService.connectToHeadUnit();
            }
            return true;
        } else if (id == R.id.action_disconnect) {
            if (protocolService != null) {
                protocolService.disconnectFromHeadUnit();
            }
            return true;
        } else if (id == R.id.action_export_logs) {
            if (protocolService != null) {
                protocolService.exportLogs();
            }
            return true;
        } else if (id == R.id.action_diagnostics) {
            showDiagnosticInfo();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Show diagnostic information dialog
     */
    private void showDiagnosticInfo() {
        if (protocolService != null) {
            String diagnosticInfo = protocolService.getDiagnosticInfo();
            
            new AlertDialog.Builder(this)
                    .setTitle("Diagnostic Information")
                    .setMessage(diagnosticInfo)
                    .setPositiveButton("OK", null)
                    .show();
        }
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
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        
        super.onDestroy();
    }
    
    // ProtocolStateListener implementation
    
    @Override
    public void onStateChanged(boolean connected, String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            // Update UI based on connection state
        });
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
        });
    }
}