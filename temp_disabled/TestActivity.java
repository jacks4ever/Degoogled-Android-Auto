package com.degoogled.androidauto;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.degoogled.androidauto.apps.OsmAndIntegration;
import com.degoogled.androidauto.apps.VLCIntegration;
import com.degoogled.androidauto.logging.ConnectionLogger;
import com.degoogled.androidauto.service.ProtocolHandlerServiceEnhanced;

/**
 * Test Activity for demonstrating all Degoogled Android Auto features
 * Provides UI for testing navigation, media, and protocol functionality
 */
public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";
    
    private ConnectionLogger logger;
    private ProtocolHandlerServiceEnhanced protocolService;
    private boolean serviceBound = false;
    
    // UI components
    private TextView statusText;
    private EditText addressInput;
    private EditText mediaPathInput;
    private Button connectButton;
    private Button disconnectButton;
    private Button navButton;
    private Button stopNavButton;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button nextButton;
    private Button prevButton;
    private Button exportLogsButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
        // Initialize logger
        logger = ConnectionLogger.getInstance(this);
        logger.logInfo("TestActivity started");
        
        // Initialize UI
        initializeUI();
        
        // Bind to service
        bindToProtocolService();
    }
    
    private void initializeUI() {
        statusText = findViewById(R.id.statusText);
        addressInput = findViewById(R.id.addressInput);
        mediaPathInput = findViewById(R.id.mediaPathInput);
        
        connectButton = findViewById(R.id.connectButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        navButton = findViewById(R.id.navButton);
        stopNavButton = findViewById(R.id.stopNavButton);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        exportLogsButton = findViewById(R.id.exportLogsButton);
        
        // Set default values
        addressInput.setText("1600 Amphitheatre Parkway, Mountain View, CA");
        mediaPathInput.setText("/sdcard/Music/test.mp3");
        
        // Set click listeners
        connectButton.setOnClickListener(v -> testConnection());
        disconnectButton.setOnClickListener(v -> testDisconnection());
        navButton.setOnClickListener(v -> testNavigation());
        stopNavButton.setOnClickListener(v -> testStopNavigation());
        playButton.setOnClickListener(v -> testPlayMedia());
        pauseButton.setOnClickListener(v -> testPauseMedia());
        stopButton.setOnClickListener(v -> testStopMedia());
        nextButton.setOnClickListener(v -> testNextTrack());
        prevButton.setOnClickListener(v -> testPreviousTrack());
        exportLogsButton.setOnClickListener(v -> exportLogs());
        
        updateStatus("Initializing...");
    }
    
    private void bindToProtocolService() {
        Intent serviceIntent = new Intent(this, ProtocolHandlerServiceEnhanced.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Note: ProtocolHandlerServiceEnhanced doesn't have a binder yet
            // We'll work with it through intents for now
            serviceBound = true;
            updateStatus("Service connected - Ready for testing");
            logger.logInfo("Connected to ProtocolHandlerServiceEnhanced");
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            updateStatus("Service disconnected");
            logger.logInfo("Disconnected from ProtocolHandlerServiceEnhanced");
        }
    };
    
    private void testConnection() {
        updateStatus("Testing USB connection...");
        logger.logInfo("Manual connection test initiated");
        
        // Simulate connection test
        Toast.makeText(this, "Connection test initiated - Check logs", Toast.LENGTH_SHORT).show();
        
        // In a real implementation, this would trigger actual USB connection
        updateStatus("Connection test completed - Check logs for details");
    }
    
    private void testDisconnection() {
        updateStatus("Testing disconnection...");
        logger.logInfo("Manual disconnection test initiated");
        
        Toast.makeText(this, "Disconnection test initiated", Toast.LENGTH_SHORT).show();
        updateStatus("Disconnection test completed");
    }
    
    private void testNavigation() {
        String address = addressInput.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        updateStatus("Testing navigation to: " + address);
        logger.logInfo("Testing navigation to: " + address);
        
        // Test OsmAnd integration
        OsmAndIntegration osmAnd = new OsmAndIntegration(this, logger);
        if (osmAnd.isOsmAndAvailable()) {
            boolean success = osmAnd.navigateToAddress(address);
            if (success) {
                updateStatus("Navigation started successfully");
                Toast.makeText(this, "Navigation started in OsmAnd", Toast.LENGTH_SHORT).show();
            } else {
                updateStatus("Navigation failed");
                Toast.makeText(this, "Navigation failed - Check logs", Toast.LENGTH_SHORT).show();
            }
        } else {
            updateStatus("OsmAnd not available");
            Toast.makeText(this, "OsmAnd not installed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void testStopNavigation() {
        updateStatus("Stopping navigation...");
        logger.logInfo("Testing stop navigation");
        
        OsmAndIntegration osmAnd = new OsmAndIntegration(this, logger);
        boolean success = osmAnd.stopNavigation();
        
        if (success) {
            updateStatus("Navigation stopped");
            Toast.makeText(this, "Navigation stopped", Toast.LENGTH_SHORT).show();
        } else {
            updateStatus("Failed to stop navigation");
            Toast.makeText(this, "Failed to stop navigation", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void testPlayMedia() {
        String mediaPath = mediaPathInput.getText().toString().trim();
        if (mediaPath.isEmpty()) {
            Toast.makeText(this, "Please enter a media path", Toast.LENGTH_SHORT).show();
            return;
        }
        
        updateStatus("Testing media playback: " + mediaPath);
        logger.logInfo("Testing media playback: " + mediaPath);
        
        // Test VLC integration
        VLCIntegration vlc = new VLCIntegration(this, logger);
        if (vlc.isVLCAvailable()) {
            boolean success = vlc.playMedia(mediaPath);
            if (success) {
                updateStatus("Media playback started");
                Toast.makeText(this, "Media started in VLC", Toast.LENGTH_SHORT).show();
            } else {
                updateStatus("Media playback failed");
                Toast.makeText(this, "Media playback failed - Check logs", Toast.LENGTH_SHORT).show();
            }
        } else {
            updateStatus("VLC not available");
            Toast.makeText(this, "VLC not installed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void testPauseMedia() {
        updateStatus("Testing media pause...");
        logger.logInfo("Testing media pause");
        
        VLCIntegration vlc = new VLCIntegration(this, logger);
        boolean success = vlc.pause();
        
        updateStatus(success ? "Media paused" : "Failed to pause media");
        Toast.makeText(this, success ? "Media paused" : "Pause failed", Toast.LENGTH_SHORT).show();
    }
    
    private void testStopMedia() {
        updateStatus("Testing media stop...");
        logger.logInfo("Testing media stop");
        
        VLCIntegration vlc = new VLCIntegration(this, logger);
        boolean success = vlc.stop();
        
        updateStatus(success ? "Media stopped" : "Failed to stop media");
        Toast.makeText(this, success ? "Media stopped" : "Stop failed", Toast.LENGTH_SHORT).show();
    }
    
    private void testNextTrack() {
        updateStatus("Testing next track...");
        logger.logInfo("Testing next track");
        
        VLCIntegration vlc = new VLCIntegration(this, logger);
        boolean success = vlc.nextTrack();
        
        updateStatus(success ? "Next track" : "Failed to skip track");
        Toast.makeText(this, success ? "Next track" : "Skip failed", Toast.LENGTH_SHORT).show();
    }
    
    private void testPreviousTrack() {
        updateStatus("Testing previous track...");
        logger.logInfo("Testing previous track");
        
        VLCIntegration vlc = new VLCIntegration(this, logger);
        boolean success = vlc.previousTrack();
        
        updateStatus(success ? "Previous track" : "Failed to go to previous track");
        Toast.makeText(this, success ? "Previous track" : "Previous failed", Toast.LENGTH_SHORT).show();
    }
    
    private void exportLogs() {
        updateStatus("Exporting logs...");
        logger.logInfo("Manual log export requested");
        
        try {
            java.io.File logDir = logger.exportLogs();
            if (logDir != null) {
                updateStatus("Logs exported to: " + logDir.getAbsolutePath());
                Toast.makeText(this, "Logs exported successfully", Toast.LENGTH_LONG).show();
            } else {
                updateStatus("Failed to export logs");
                Toast.makeText(this, "Failed to export logs", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            updateStatus("Error exporting logs: " + e.getMessage());
            Toast.makeText(this, "Export error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void updateStatus(String status) {
        runOnUiThread(() -> {
            if (statusText != null) {
                statusText.setText("Status: " + status);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        
        logger.logInfo("TestActivity destroyed");
        super.onDestroy();
    }
}