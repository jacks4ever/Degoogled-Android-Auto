package com.degoogled.androidauto.protocol;

import android.hardware.usb.UsbAccessory;

import com.degoogled.androidauto.logging.ConnectionLogger;
import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;
import com.degoogled.androidauto.protocol.messages.auth.AuthChallengeRequest;
import com.degoogled.androidauto.protocol.messages.auth.AuthChallengeResponse;
import com.degoogled.androidauto.protocol.messages.auth.AuthCompleteRequest;
import com.degoogled.androidauto.protocol.messages.auth.AuthCompleteResponse;
import com.degoogled.androidauto.protocol.messages.auth.AuthStartRequest;
import com.degoogled.androidauto.protocol.messages.auth.AuthStartResponse;
import com.degoogled.androidauto.protocol.messages.control.ControlVersionRequest;
import com.degoogled.androidauto.protocol.messages.control.ControlVersionResponse;
import com.degoogled.androidauto.protocol.messages.control.ServiceDiscoveryRequest;
import com.degoogled.androidauto.protocol.messages.control.ServiceDiscoveryResponse;
import com.degoogled.androidauto.utils.LogManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the Android Auto protocol session.
 * Handles authentication, service discovery, and channel setup.
 */
public class ProtocolSession implements UsbConnection.UsbConnectionListener {
    private static final String TAG = "ProtocolSession";
    
    // Protocol versions
    private static final int PROTOCOL_VERSION = 1;
    private static final int MAX_PROTOCOL_VERSION = 2;
    
    // Authentication methods
    private static final int AUTH_METHOD_NONE = 0;
    private static final int AUTH_METHOD_SIMPLE = 1;
    private static final int AUTH_METHOD_STANDARD = 2;
    
    // Session states
    private enum SessionState {
        DISCONNECTED,
        CONNECTED,
        VERSION_NEGOTIATED,
        AUTHENTICATED,
        SERVICES_DISCOVERED,
        READY
    }
    
    private final UsbConnection usbConnection;
    private final ConnectionLogger logger;
    private final ProtocolHandler protocolHandler;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    private SessionState state = SessionState.DISCONNECTED;
    private int negotiatedVersion = 0;
    private int authMethod = AUTH_METHOD_NONE;
    private long deviceId;
    private List<ServiceDiscoveryResponse.Service> availableServices = new ArrayList<>();
    
    private ProtocolSessionListener listener;
    
    /**
     * Create a new protocol session
     */
    public ProtocolSession(UsbConnection usbConnection, ConnectionLogger logger, ProtocolHandler protocolHandler) {
        this.usbConnection = usbConnection;
        this.logger = logger;
        this.protocolHandler = protocolHandler;
        
        usbConnection.setListener(this);
        
        // Generate a random device ID
        SecureRandom random = new SecureRandom();
        deviceId = random.nextLong();
    }
    
    /**
     * Set the session listener
     */
    public void setListener(ProtocolSessionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Initialize the protocol session
     */
    public void initialize() {
        if (isInitialized.getAndSet(true)) {
            return;
        }
        
        LogManager.i(TAG, "Initializing protocol session");
        logger.logConnection("Initializing protocol session");
    }
    
    /**
     * Start the protocol handshake
     */
    public void startHandshake() {
        if (state != SessionState.CONNECTED) {
            LogManager.w(TAG, "Cannot start handshake, not connected");
            return;
        }
        
        LogManager.i(TAG, "Starting protocol handshake");
        logger.logConnection("Starting protocol handshake");
        
        // Send version request
        ControlVersionRequest versionRequest = new ControlVersionRequest(PROTOCOL_VERSION, MAX_PROTOCOL_VERSION);
        usbConnection.sendMessage(versionRequest);
    }
    
    /**
     * Start the authentication process
     */
    private void startAuthentication() {
        if (state != SessionState.VERSION_NEGOTIATED) {
            LogManager.w(TAG, "Cannot start authentication, version not negotiated");
            return;
        }
        
        LogManager.i(TAG, "Starting authentication");
        logger.logConnection("Starting authentication");
        
        // Try different authentication methods based on the protocol handler's capabilities
        if (protocolHandler.supportsStandardAuth()) {
            authMethod = AUTH_METHOD_STANDARD;
        } else if (protocolHandler.supportsSimpleAuth()) {
            authMethod = AUTH_METHOD_SIMPLE;
        } else {
            authMethod = AUTH_METHOD_NONE;
        }
        
        // Send authentication start request
        AuthStartRequest authStartRequest = new AuthStartRequest(negotiatedVersion, authMethod, deviceId);
        usbConnection.sendMessage(authStartRequest);
    }
    
    /**
     * Handle an authentication challenge
     */
    private void handleAuthChallenge(AuthChallengeRequest request) {
        LogManager.d(TAG, "Handling auth challenge");
        
        byte[] challenge = request.getChallenge();
        byte[] response;
        
        switch (authMethod) {
            case AUTH_METHOD_STANDARD:
                response = protocolHandler.generateStandardAuthResponse(challenge);
                break;
                
            case AUTH_METHOD_SIMPLE:
                response = protocolHandler.generateSimpleAuthResponse(challenge);
                break;
                
            case AUTH_METHOD_NONE:
            default:
                // For no authentication, just echo back the challenge
                response = challenge;
                break;
        }
        
        // Send authentication challenge response
        AuthChallengeResponse challengeResponse = new AuthChallengeResponse(response);
        usbConnection.sendMessage(challengeResponse);
    }
    
    /**
     * Complete the authentication process
     */
    private void completeAuthentication() {
        LogManager.d(TAG, "Completing authentication");
        
        // Send authentication complete request
        AuthCompleteRequest authCompleteRequest = new AuthCompleteRequest(MessageTypes.STATUS_OK);
        usbConnection.sendMessage(authCompleteRequest);
    }
    
    /**
     * Start service discovery
     */
    private void startServiceDiscovery() {
        if (state != SessionState.AUTHENTICATED) {
            LogManager.w(TAG, "Cannot start service discovery, not authenticated");
            return;
        }
        
        LogManager.i(TAG, "Starting service discovery");
        logger.logConnection("Starting service discovery");
        
        // Send service discovery request
        ServiceDiscoveryRequest discoveryRequest = new ServiceDiscoveryRequest();
        usbConnection.sendMessage(discoveryRequest);
    }
    
    /**
     * Handle a service discovery response
     */
    private void handleServiceDiscovery(ServiceDiscoveryResponse response) {
        if (!response.isSuccess()) {
            LogManager.e(TAG, "Service discovery failed with status: " + response.getStatus());
            logger.logError("Service discovery failed: " + response.getStatus());
            return;
        }
        
        availableServices = response.getServices();
        
        LogManager.i(TAG, "Discovered " + availableServices.size() + " services");
        logger.logConnection("Discovered " + availableServices.size() + " services");
        
        for (ServiceDiscoveryResponse.Service service : availableServices) {
            LogManager.d(TAG, "Service: " + service);
        }
        
        state = SessionState.SERVICES_DISCOVERED;
        
        // Set up channels for the discovered services
        protocolHandler.setupChannels(availableServices, usbConnection);
        
        state = SessionState.READY;
        
        if (listener != null) {
            listener.onSessionReady();
        }
    }
    
    /**
     * Handle a message received from the USB connection
     */
    @Override
    public void onMessageReceived(Message message) {
        LogManager.d(TAG, "Received message: " + message);
        
        int channelId = message.getChannelId();
        int messageType = message.getMessageType();
        
        // Handle messages based on the current state and message type
        switch (channelId) {
            case MessageTypes.CHANNEL_CONTROL:
                handleControlMessage(message, messageType);
                break;
                
            case MessageTypes.CHANNEL_AUTH:
                handleAuthMessage(message, messageType);
                break;
                
            default:
                // Forward other messages to the protocol handler
                protocolHandler.handleMessage(message);
                break;
        }
    }
    
    /**
     * Handle a control channel message
     */
    private void handleControlMessage(Message message, int messageType) {
        switch (messageType) {
            case MessageTypes.CONTROL_MSG_VERSION_RESPONSE:
                ControlVersionResponse versionResponse = (ControlVersionResponse) message;
                
                if (versionResponse.isSuccess()) {
                    negotiatedVersion = versionResponse.getProtocolVersion();
                    LogManager.i(TAG, "Version negotiated: " + negotiatedVersion);
                    logger.logConnection("Version negotiated: " + negotiatedVersion);
                    
                    state = SessionState.VERSION_NEGOTIATED;
                    startAuthentication();
                } else {
                    LogManager.e(TAG, "Version negotiation failed with status: " + versionResponse.getStatus());
                    logger.logError("Version negotiation failed: " + versionResponse.getStatus());
                }
                break;
                
            case MessageTypes.CONTROL_MSG_SERVICE_DISCOVERY_RESPONSE:
                ServiceDiscoveryResponse discoveryResponse = (ServiceDiscoveryResponse) message;
                handleServiceDiscovery(discoveryResponse);
                break;
                
            default:
                // Forward other control messages to the protocol handler
                protocolHandler.handleControlMessage(message, messageType);
                break;
        }
    }
    
    /**
     * Handle an authentication channel message
     */
    private void handleAuthMessage(Message message, int messageType) {
        switch (messageType) {
            case MessageTypes.AUTH_MSG_START_RESPONSE:
                AuthStartResponse startResponse = (AuthStartResponse) message;
                
                if (startResponse.isSuccess()) {
                    LogManager.i(TAG, "Authentication start successful");
                    logger.logConnection("Authentication start successful");
                } else {
                    LogManager.e(TAG, "Authentication start failed with status: " + startResponse.getStatus());
                    logger.logError("Authentication start failed: " + startResponse.getStatus());
                    
                    // Try a different authentication method
                    if (authMethod == AUTH_METHOD_STANDARD) {
                        LogManager.i(TAG, "Falling back to simple authentication");
                        authMethod = AUTH_METHOD_SIMPLE;
                        AuthStartRequest authStartRequest = new AuthStartRequest(negotiatedVersion, authMethod, deviceId);
                        usbConnection.sendMessage(authStartRequest);
                    } else if (authMethod == AUTH_METHOD_SIMPLE) {
                        LogManager.i(TAG, "Falling back to no authentication");
                        authMethod = AUTH_METHOD_NONE;
                        AuthStartRequest authStartRequest = new AuthStartRequest(negotiatedVersion, authMethod, deviceId);
                        usbConnection.sendMessage(authStartRequest);
                    }
                }
                break;
                
            case MessageTypes.AUTH_MSG_CHALLENGE_REQUEST:
                AuthChallengeRequest challengeRequest = (AuthChallengeRequest) message;
                handleAuthChallenge(challengeRequest);
                break;
                
            case MessageTypes.AUTH_MSG_COMPLETE_RESPONSE:
                AuthCompleteResponse completeResponse = (AuthCompleteResponse) message;
                
                if (completeResponse.isSuccess()) {
                    LogManager.i(TAG, "Authentication complete");
                    logger.logConnection("Authentication complete");
                    
                    state = SessionState.AUTHENTICATED;
                    startServiceDiscovery();
                } else {
                    LogManager.e(TAG, "Authentication completion failed with status: " + completeResponse.getStatus());
                    logger.logError("Authentication completion failed: " + completeResponse.getStatus());
                }
                break;
                
            default:
                // Forward other auth messages to the protocol handler
                protocolHandler.handleAuthMessage(message, messageType);
                break;
        }
    }
    
    /**
     * Handle a connection to a USB accessory
     */
    @Override
    public void onConnected(UsbAccessory accessory) {
        LogManager.i(TAG, "Connected to accessory: " + accessory.getModel());
        logger.logConnection("Connected to accessory: " + accessory.getManufacturer() + " " + accessory.getModel());
        
        state = SessionState.CONNECTED;
        
        if (listener != null) {
            listener.onConnected(accessory);
        }
        
        // Start the protocol handshake
        startHandshake();
    }
    
    /**
     * Handle a disconnection from the USB accessory
     */
    @Override
    public void onDisconnected() {
        LogManager.i(TAG, "Disconnected from accessory");
        logger.logConnection("Disconnected from accessory");
        
        state = SessionState.DISCONNECTED;
        
        if (listener != null) {
            listener.onDisconnected();
        }
    }
    
    /**
     * Handle an error from the USB connection
     */
    @Override
    public void onError(Exception e) {
        LogManager.e(TAG, "USB connection error", e);
        logger.logError("USB connection error", e);
        
        if (listener != null) {
            listener.onError(e);
        }
    }
    
    /**
     * Generate a simple authentication response
     */
    private byte[] generateSimpleAuthResponse(byte[] challenge) {
        try {
            // Simple authentication just uses SHA-256 of the challenge
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(challenge);
        } catch (NoSuchAlgorithmException e) {
            LogManager.e(TAG, "Error generating auth response", e);
            return challenge; // Fallback to echo
        }
    }
    
    /**
     * Interface for protocol session events
     */
    public interface ProtocolSessionListener {
        void onConnected(UsbAccessory accessory);
        void onDisconnected();
        void onSessionReady();
        void onError(Exception e);
    }
}