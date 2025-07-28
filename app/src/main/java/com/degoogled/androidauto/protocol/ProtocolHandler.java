package com.degoogled.androidauto.protocol;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.degoogled.androidauto.utils.LogManager;
import com.degoogled.androidauto.utils.UsbDeviceManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simplified Android Auto protocol handler that integrates with the enhanced features
 * while maintaining compatibility with the existing codebase.
 */
public class ProtocolHandler implements UsbDeviceManager.UsbConnectionListener {
    private static final String TAG = "ProtocolHandler";
    
    // Protocol constants
    private static final int MSG_CONTROL = 0x01;
    private static final int MSG_AUTH = 0x02;
    private static final int MSG_MEDIA = 0x03;
    private static final int MSG_NAV = 0x04;
    private static final int MSG_VOICE = 0x05;
    private static final int MSG_PHONE = 0x06;
    
    // Authentication methods
    private static final int AUTH_METHOD_STANDARD = 0x01;
    private static final int AUTH_METHOD_NISSAN = 0x02;
    private static final int AUTH_METHOD_GENERIC = 0x03;
    
    // Connection states
    private enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        AUTHENTICATING,
        CONNECTED,
        ERROR
    }
    
    private final Context context;
    private final UsbDeviceManager usbManager;
    private final Handler mainHandler;
    private final ExecutorService executorService;
    private ProtocolListener protocolListener;
    private ConnectionState state = ConnectionState.DISCONNECTED;
    private int currentAuthMethod = AUTH_METHOD_STANDARD;
    private int authAttempts = 0;
    private static final int MAX_AUTH_ATTEMPTS = 3;
    
    public interface ProtocolListener {
        void onConnectionStateChanged(boolean connected, String message);
        void onProtocolMessage(int messageType, byte[] data);
        void onProtocolError(String error);
    }
    
    public ProtocolHandler(Context context) {
        this.context = context;
        this.usbManager = new UsbDeviceManager(context);
        this.usbManager.setConnectionListener(this);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();
        
        LogManager.i(TAG, "ProtocolHandler initialized");
    }
    
    /**
     * Set a listener for protocol events
     */
    public void setProtocolListener(ProtocolListener listener) {
        this.protocolListener = listener;
    }
    
    /**
     * Start the connection process
     */
    public void connect() {
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.ERROR) {
            setState(ConnectionState.CONNECTING);
            LogManager.i(TAG, "Starting connection process");
            usbManager.checkForConnectedAccessory();
        } else {
            LogManager.w(TAG, "Cannot connect - already in state: " + state);
        }
    }
    
    /**
     * Disconnect from the head unit
     */
    public void disconnect() {
        LogManager.i(TAG, "Disconnecting from head unit");
        usbManager.closeAccessory();
        setState(ConnectionState.DISCONNECTED);
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        disconnect();
        usbManager.cleanup();
        executorService.shutdown();
    }
    
    /**
     * Set the connection state and notify listeners
     */
    private void setState(ConnectionState newState) {
        if (state != newState) {
            LogManager.i(TAG, "State changed: " + state + " -> " + newState);
            state = newState;
            
            if (protocolListener != null) {
                boolean isConnected = (state == ConnectionState.CONNECTED);
                String message = getStateMessage();
                mainHandler.post(() -> protocolListener.onConnectionStateChanged(isConnected, message));
            }
        }
    }
    
    /**
     * Get a user-friendly message for the current state
     */
    private String getStateMessage() {
        switch (state) {
            case DISCONNECTED:
                return "Disconnected from head unit";
            case CONNECTING:
                return "Connecting to head unit...";
            case AUTHENTICATING:
                return "Authenticating with head unit...";
            case CONNECTED:
                return "Connected to head unit";
            case ERROR:
                return "Connection error";
            default:
                return "Unknown state";
        }
    }
    
    /**
     * Start the authentication process
     */
    private void startAuthentication() {
        setState(ConnectionState.AUTHENTICATING);
        authAttempts = 0;
        
        // Try the standard authentication method first
        currentAuthMethod = AUTH_METHOD_STANDARD;
        sendAuthMessage();
    }
    
    /**
     * Send an authentication message using the current method
     */
    private void sendAuthMessage() {
        LogManager.i(TAG, "Sending authentication message (method: " + currentAuthMethod + ", attempt: " + (authAttempts + 1) + ")");
        
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) MSG_AUTH);
        buffer.put((byte) currentAuthMethod);
        
        // Add authentication data based on the method
        switch (currentAuthMethod) {
            case AUTH_METHOD_STANDARD:
                // Standard method - mimics Google's protocol without requiring Google services
                buffer.put((byte) 0x01);  // Version
                buffer.put((byte) 0x00);  // Flags
                buffer.putInt(0x12345678); // Device ID (placeholder)
                break;
                
            case AUTH_METHOD_NISSAN:
                // Nissan-specific method based on observed patterns
                buffer.put((byte) 0x02);  // Version
                buffer.put((byte) 0x01);  // Flags
                buffer.putInt(0x2B240001); // Nissan-specific ID
                break;
                
            case AUTH_METHOD_GENERIC:
                // Generic fallback method
                buffer.put((byte) 0x01);  // Version
                buffer.put((byte) 0x02);  // Flags
                buffer.putInt(0x00000000); // Generic ID
                break;
        }
        
        // Send the authentication message
        usbManager.sendData(buffer.array());
        
        // Increment attempt counter
        authAttempts++;
    }
    
    /**
     * Try the next authentication method if available
     */
    private void tryNextAuthMethod() {
        if (authAttempts >= MAX_AUTH_ATTEMPTS) {
            // We've tried the current method enough times, switch to the next one
            switch (currentAuthMethod) {
                case AUTH_METHOD_STANDARD:
                    currentAuthMethod = AUTH_METHOD_NISSAN;
                    authAttempts = 0;
                    sendAuthMessage();
                    break;
                    
                case AUTH_METHOD_NISSAN:
                    currentAuthMethod = AUTH_METHOD_GENERIC;
                    authAttempts = 0;
                    sendAuthMessage();
                    break;
                    
                case AUTH_METHOD_GENERIC:
                    // We've tried all methods
                    LogManager.e(TAG, "Authentication failed after trying all methods");
                    setState(ConnectionState.ERROR);
                    if (protocolListener != null) {
                        mainHandler.post(() -> protocolListener.onProtocolError("Authentication failed"));
                    }
                    break;
            }
        } else {
            // Try the current method again
            sendAuthMessage();
        }
    }
    
    /**
     * Process a received message from the head unit
     */
    private void processMessage(byte[] data, int length) {
        if (length < 2) {
            LogManager.w(TAG, "Received message too short: " + length + " bytes");
            return;
        }
        
        int messageType = data[0] & 0xFF;
        LogManager.v(TAG, "Processing message type: 0x" + Integer.toHexString(messageType) + ", length: " + length);
        
        switch (messageType) {
            case MSG_CONTROL:
                processControlMessage(data, length);
                break;
                
            case MSG_AUTH:
                processAuthMessage(data, length);
                break;
                
            default:
                // Forward other message types to the listener
                if (state == ConnectionState.CONNECTED && protocolListener != null) {
                    final byte[] messageCopy = Arrays.copyOf(data, length);
                    mainHandler.post(() -> protocolListener.onProtocolMessage(messageType, messageCopy));
                }
                break;
        }
    }
    
    /**
     * Process a control message
     */
    private void processControlMessage(byte[] data, int length) {
        if (length < 3) {
            return;
        }
        
        int controlType = data[1] & 0xFF;
        LogManager.d(TAG, "Control message: 0x" + Integer.toHexString(controlType));
        
        // Handle different control messages
        switch (controlType) {
            case 0x01:  // Version info
                LogManager.i(TAG, "Received version info from head unit");
                // If we're not connected yet, start authentication
                if (state == ConnectionState.CONNECTING) {
                    startAuthentication();
                }
                break;
                
            case 0x02:  // Status update
                LogManager.i(TAG, "Received status update from head unit");
                break;
                
            case 0xFF:  // Error
                LogManager.w(TAG, "Received error from head unit");
                break;
        }
    }
    
    /**
     * Process an authentication message
     */
    private void processAuthMessage(byte[] data, int length) {
        if (length < 3) {
            return;
        }
        
        int authStatus = data[1] & 0xFF;
        LogManager.d(TAG, "Auth message status: 0x" + Integer.toHexString(authStatus));
        
        switch (authStatus) {
            case 0x00:  // Success
                LogManager.i(TAG, "Authentication successful (method: " + currentAuthMethod + ")");
                setState(ConnectionState.CONNECTED);
                break;
                
            case 0x01:  // Challenge
                LogManager.i(TAG, "Received authentication challenge");
                // Extract challenge data and respond
                // This is a simplified version
                respondToChallenge(data, length);
                break;
                
            case 0xFF:  // Failure
                LogManager.w(TAG, "Authentication failed, trying next method");
                tryNextAuthMethod();
                break;
                
            default:
                LogManager.w(TAG, "Unknown authentication status: 0x" + Integer.toHexString(authStatus));
                tryNextAuthMethod();
                break;
        }
    }
    
    /**
     * Respond to an authentication challenge
     */
    private void respondToChallenge(byte[] challenge, int length) {
        // This is a simplified implementation
        // In a real implementation, we would process the challenge data
        // and generate an appropriate response
        
        LogManager.i(TAG, "Sending challenge response");
        
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) MSG_AUTH);
        buffer.put((byte) 0x02);  // Challenge response
        buffer.put((byte) currentAuthMethod);
        
        // Add some response data
        // In a real implementation, this would be calculated based on the challenge
        for (int i = 0; i < 8; i++) {
            buffer.put((byte) (i ^ 0xAA));
        }
        
        usbManager.sendData(buffer.array());
    }
    
    /**
     * Send a message to the head unit
     */
    public boolean sendMessage(int messageType, byte[] data) {
        if (state != ConnectionState.CONNECTED) {
            LogManager.w(TAG, "Cannot send message - not connected");
            return false;
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 1);
        buffer.put((byte) messageType);
        buffer.put(data);
        
        return usbManager.sendData(buffer.array());
    }
    
    // UsbConnectionListener implementation
    
    @Override
    public void onUsbConnected(boolean success) {
        if (success) {
            LogManager.i(TAG, "USB connected, waiting for head unit communication");
            // The state will be updated when we receive the first message
        } else {
            LogManager.e(TAG, "USB connection failed");
            setState(ConnectionState.ERROR);
        }
    }
    
    @Override
    public void onUsbDisconnected() {
        LogManager.i(TAG, "USB disconnected");
        setState(ConnectionState.DISCONNECTED);
    }
    
    @Override
    public void onUsbData(byte[] data, int length) {
        // Process the data on a background thread
        executorService.execute(() -> processMessage(data, length));
    }
    
    @Override
    public void onUsbError(String error) {
        LogManager.e(TAG, "USB error: " + error);
        setState(ConnectionState.ERROR);
        if (protocolListener != null) {
            mainHandler.post(() -> protocolListener.onProtocolError(error));
        }
    }
    
    /**
     * Get diagnostic information about the current connection
     */
    public String getDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Connection State: ").append(state).append("\n");
        info.append("Current Auth Method: ").append(currentAuthMethod).append("\n");
        info.append("Auth Attempts: ").append(authAttempts).append("\n");
        
        // Add USB device information
        info.append("\nConnected USB Devices:\n");
        for (String deviceInfo : usbManager.getConnectedDeviceInfo()) {
            info.append(deviceInfo).append("\n");
        }
        
        return info.toString();
    }
}