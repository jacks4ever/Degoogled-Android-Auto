package com.degoogled.androidauto.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.degoogled.androidauto.MainActivity;
import com.degoogled.androidauto.R;
import com.degoogled.androidauto.protocol.ProtocolHandler;
import com.degoogled.androidauto.utils.LogManager;

import java.io.File;
import java.io.IOException;

/**
 * Service that handles the Android Auto protocol communication with the head unit.
 * Enhanced with Nissan Pathfinder compatibility and verbose logging.
 */
public class ProtocolHandlerService extends Service implements ProtocolHandler.ProtocolListener {
    private static final String TAG = "ProtocolHandlerService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "DegoogledAndroidAutoChannel";
    
    private ProtocolHandler protocolHandler;
    private final IBinder binder = new LocalBinder();
    private ProtocolStateListener stateListener;
    
    public interface ProtocolStateListener {
        void onStateChanged(boolean connected, String message);
        void onError(String error);
    }
    
    public class LocalBinder extends Binder {
        public ProtocolHandlerService getService() {
            return ProtocolHandlerService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        LogManager.i(TAG, "ProtocolHandlerService created");
        
        // Create notification channel for foreground service
        createNotificationChannel();
        
        // Initialize the protocol handler
        protocolHandler = new ProtocolHandler(this);
        protocolHandler.setProtocolListener(this);
        
        // Start as a foreground service
        startForeground(NOTIFICATION_ID, createNotification("Initializing..."));
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.i(TAG, "ProtocolHandlerService started");
        
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            LogManager.d(TAG, "Service action: " + action);
            
            switch (action) {
                case "CONNECT":
                    connectToHeadUnit();
                    break;
                    
                case "DISCONNECT":
                    disconnectFromHeadUnit();
                    break;
                    
                case "EXPORT_LOGS":
                    exportLogs();
                    break;
            }
        }
        
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        LogManager.i(TAG, "ProtocolHandlerService destroyed");
        
        if (protocolHandler != null) {
            protocolHandler.cleanup();
        }
        
        super.onDestroy();
    }
    
    /**
     * Set a listener for protocol state changes
     */
    public void setStateListener(ProtocolStateListener listener) {
        this.stateListener = listener;
    }
    
    /**
     * Connect to the head unit
     */
    public void connectToHeadUnit() {
        LogManager.i(TAG, "Connecting to head unit");
        updateNotification("Connecting to head unit...");
        
        if (protocolHandler != null) {
            protocolHandler.connect();
        }
    }
    
    /**
     * Disconnect from the head unit
     */
    public void disconnectFromHeadUnit() {
        LogManager.i(TAG, "Disconnecting from head unit");
        updateNotification("Disconnecting from head unit...");
        
        if (protocolHandler != null) {
            protocolHandler.disconnect();
        }
    }
    
    /**
     * Export logs to a file
     */
    public void exportLogs() {
        try {
            File logFile = LogManager.exportLogs(this);
            String message = "Logs exported to: " + logFile.getAbsolutePath();
            LogManager.i(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            String error = "Failed to export logs: " + e.getMessage();
            LogManager.e(TAG, error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Get diagnostic information about the current connection
     */
    public String getDiagnosticInfo() {
        if (protocolHandler != null) {
            return protocolHandler.getDiagnosticInfo();
        }
        return "Protocol handler not initialized";
    }
    
    /**
     * Create the notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Degoogled Android Auto",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Degoogled Android Auto service notifications");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Create a notification for the foreground service
     */
    private Notification createNotification(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Degoogled Android Auto")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .build();
    }
    
    /**
     * Update the foreground service notification
     */
    private void updateNotification(String message) {
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, createNotification(message));
        }
    }
    
    // ProtocolListener implementation
    
    @Override
    public void onConnectionStateChanged(boolean connected, String message) {
        LogManager.i(TAG, "Connection state changed: " + message);
        updateNotification(message);
        
        if (stateListener != null) {
            stateListener.onStateChanged(connected, message);
        }
    }
    
    @Override
    public void onProtocolMessage(int messageType, byte[] data) {
        LogManager.v(TAG, "Protocol message received: type=" + messageType + ", length=" + data.length);
        // Process different message types as needed
    }
    
    @Override
    public void onProtocolError(String error) {
        LogManager.e(TAG, "Protocol error: " + error);
        updateNotification("Error: " + error);
        
        if (stateListener != null) {
            stateListener.onError(error);
        }
    }
}