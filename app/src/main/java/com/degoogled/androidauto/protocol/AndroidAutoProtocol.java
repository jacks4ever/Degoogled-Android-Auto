package com.degoogled.androidauto.protocol;

import android.util.Log;

import com.degoogled.androidauto.logging.ConnectionLogger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Android Auto Protocol (AAP) implementation
 * Handles the core protocol communication between device and head unit
 */
public class AndroidAutoProtocol {
    private static final String TAG = "AndroidAutoProtocol";
    
    // Protocol constants
    public static final int AAP_VERSION_1_0 = 0x0100;
    public static final int AAP_VERSION_2_0 = 0x0200;
    
    // Message types
    public static final int MSG_VERSION_REQUEST = 0x0001;
    public static final int MSG_VERSION_RESPONSE = 0x0002;
    public static final int MSG_SSL_HANDSHAKE = 0x0003;
    public static final int MSG_AUTH_COMPLETE = 0x0004;
    public static final int MSG_SERVICE_DISCOVERY_REQUEST = 0x0005;
    public static final int MSG_SERVICE_DISCOVERY_RESPONSE = 0x0006;
    public static final int MSG_CHANNEL_OPEN_REQUEST = 0x0007;
    public static final int MSG_CHANNEL_OPEN_RESPONSE = 0x0008;
    public static final int MSG_PING = 0x000B;
    public static final int MSG_PONG = 0x000C;
    public static final int MSG_NAV_FOCUS_REQUEST = 0x000D;
    public static final int MSG_NAV_FOCUS_NOTIFICATION = 0x000E;
    public static final int MSG_SHUTDOWN_REQUEST = 0x000F;
    public static final int MSG_SHUTDOWN_RESPONSE = 0x0010;
    public static final int MSG_VOICE_SESSION_REQUEST = 0x0011;
    public static final int MSG_AUDIO_FOCUS_REQUEST = 0x0012;
    public static final int MSG_AUDIO_FOCUS_NOTIFICATION = 0x0013;
    public static final int MSG_VIDEO_FOCUS_REQUEST = 0x0014;
    public static final int MSG_VIDEO_FOCUS_NOTIFICATION = 0x0015;
    
    // Channel IDs
    public static final int CHANNEL_ID_CONTROL = 0;
    public static final int CHANNEL_ID_INPUT = 1;
    public static final int CHANNEL_ID_SENSOR = 2;
    public static final int CHANNEL_ID_VIDEO = 3;
    public static final int CHANNEL_ID_MEDIA_AUDIO = 4;
    public static final int CHANNEL_ID_SPEECH_AUDIO = 5;
    public static final int CHANNEL_ID_SYSTEM_AUDIO = 6;
    public static final int CHANNEL_ID_BLUETOOTH = 7;
    
    // Service types
    public static final String SERVICE_MEDIA_PLAYBACK = "com.google.android.gms.car.media";
    public static final String SERVICE_INPUT = "com.google.android.gms.car.input";
    public static final String SERVICE_SENSOR = "com.google.android.gms.car.sensor";
    public static final String SERVICE_VIDEO = "com.google.android.gms.car.video";
    public static final String SERVICE_BLUETOOTH = "com.google.android.gms.car.bluetooth";
    public static final String SERVICE_NAVIGATION = "com.google.android.gms.car.navigation";
    
    // Message header size
    private static final int HEADER_SIZE = 8;
    
    private ConnectionLogger logger;
    private int protocolVersion = AAP_VERSION_1_0;
    private boolean isAuthenticated = false;
    
    public AndroidAutoProtocol(ConnectionLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Create a protocol message
     */
    public byte[] createMessage(int messageType, int channelId, byte[] payload) {
        int payloadLength = payload != null ? payload.length : 0;
        int totalLength = HEADER_SIZE + payloadLength;
        
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Message header
        buffer.putShort((short) messageType);
        buffer.putShort((short) channelId);
        buffer.putInt(payloadLength);
        
        // Payload
        if (payload != null) {
            buffer.put(payload);
        }
        
        byte[] message = buffer.array();
        logger.logProtocolMessage("OUT", getMessageTypeName(messageType), message, true);
        
        return message;
    }
    
    /**
     * Parse incoming protocol message
     */
    public ProtocolMessage parseMessage(byte[] data) {
        if (data.length < HEADER_SIZE) {
            logger.logError("Invalid message: too short (" + data.length + " bytes)");
            return null;
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int messageType = buffer.getShort() & 0xFFFF;
        int channelId = buffer.getShort() & 0xFFFF;
        int payloadLength = buffer.getInt();
        
        if (data.length < HEADER_SIZE + payloadLength) {
            logger.logError("Invalid message: payload length mismatch");
            return null;
        }
        
        byte[] payload = null;
        if (payloadLength > 0) {
            payload = new byte[payloadLength];
            buffer.get(payload);
        }
        
        ProtocolMessage message = new ProtocolMessage(messageType, channelId, payload);
        logger.logProtocolMessage("IN", getMessageTypeName(messageType), data, true);
        
        return message;
    }
    
    /**
     * Create version request message
     */
    public byte[] createVersionRequest() {
        ByteBuffer payload = ByteBuffer.allocate(4);
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.putShort((short) AAP_VERSION_2_0); // Preferred version
        payload.putShort((short) AAP_VERSION_1_0); // Minimum version
        
        return createMessage(MSG_VERSION_REQUEST, CHANNEL_ID_CONTROL, payload.array());
    }
    
    /**
     * Create version response message
     */
    public byte[] createVersionResponse(int version) {
        ByteBuffer payload = ByteBuffer.allocate(2);
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.putShort((short) version);
        
        this.protocolVersion = version;
        logger.logInfo("Protocol version negotiated: " + String.format("0x%04X", version));
        
        return createMessage(MSG_VERSION_RESPONSE, CHANNEL_ID_CONTROL, payload.array());
    }
    
    /**
     * Create service discovery request
     */
    public byte[] createServiceDiscoveryRequest() {
        // Request all standard services
        String[] services = {
            SERVICE_MEDIA_PLAYBACK,
            SERVICE_INPUT,
            SERVICE_SENSOR,
            SERVICE_VIDEO,
            SERVICE_BLUETOOTH,
            SERVICE_NAVIGATION
        };
        
        StringBuilder payload = new StringBuilder();
        for (String service : services) {
            payload.append(service).append("\n");
        }
        
        return createMessage(MSG_SERVICE_DISCOVERY_REQUEST, CHANNEL_ID_CONTROL, 
                           payload.toString().getBytes());
    }
    
    /**
     * Create service discovery response
     */
    public byte[] createServiceDiscoveryResponse(String[] availableServices) {
        StringBuilder payload = new StringBuilder();
        for (String service : availableServices) {
            payload.append(service).append("\n");
        }
        
        return createMessage(MSG_SERVICE_DISCOVERY_RESPONSE, CHANNEL_ID_CONTROL, 
                           payload.toString().getBytes());
    }
    
    /**
     * Create channel open request
     */
    public byte[] createChannelOpenRequest(int channelId, String serviceName) {
        ByteBuffer payload = ByteBuffer.allocate(4 + serviceName.length());
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.putInt(channelId);
        payload.put(serviceName.getBytes());
        
        return createMessage(MSG_CHANNEL_OPEN_REQUEST, CHANNEL_ID_CONTROL, payload.array());
    }
    
    /**
     * Create channel open response
     */
    public byte[] createChannelOpenResponse(int channelId, boolean success) {
        ByteBuffer payload = ByteBuffer.allocate(5);
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.putInt(channelId);
        payload.put((byte) (success ? 1 : 0));
        
        return createMessage(MSG_CHANNEL_OPEN_RESPONSE, CHANNEL_ID_CONTROL, payload.array());
    }
    
    /**
     * Create ping message
     */
    public byte[] createPing(long timestamp) {
        ByteBuffer payload = ByteBuffer.allocate(8);
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.putLong(timestamp);
        
        return createMessage(MSG_PING, CHANNEL_ID_CONTROL, payload.array());
    }
    
    /**
     * Create pong response
     */
    public byte[] createPong(long timestamp) {
        ByteBuffer payload = ByteBuffer.allocate(8);
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.putLong(timestamp);
        
        return createMessage(MSG_PONG, CHANNEL_ID_CONTROL, payload.array());
    }
    
    /**
     * Create navigation focus request
     */
    public byte[] createNavFocusRequest() {
        return createMessage(MSG_NAV_FOCUS_REQUEST, CHANNEL_ID_CONTROL, null);
    }
    
    /**
     * Create audio focus request
     */
    public byte[] createAudioFocusRequest(int focusType) {
        ByteBuffer payload = ByteBuffer.allocate(4);
        payload.order(ByteOrder.LITTLE_ENDIAN);
        payload.putInt(focusType);
        
        return createMessage(MSG_AUDIO_FOCUS_REQUEST, CHANNEL_ID_CONTROL, payload.array());
    }
    
    /**
     * Create video focus request
     */
    public byte[] createVideoFocusRequest() {
        return createMessage(MSG_VIDEO_FOCUS_REQUEST, CHANNEL_ID_CONTROL, null);
    }
    
    /**
     * Create authentication complete message
     */
    public byte[] createAuthComplete() {
        isAuthenticated = true;
        logger.logInfo("Authentication completed successfully");
        return createMessage(MSG_AUTH_COMPLETE, CHANNEL_ID_CONTROL, null);
    }
    
    /**
     * Handle incoming protocol message
     */
    public ProtocolResponse handleMessage(ProtocolMessage message) {
        switch (message.getMessageType()) {
            case MSG_VERSION_REQUEST:
                return handleVersionRequest(message);
                
            case MSG_VERSION_RESPONSE:
                return handleVersionResponse(message);
                
            case MSG_SERVICE_DISCOVERY_REQUEST:
                return handleServiceDiscoveryRequest(message);
                
            case MSG_SERVICE_DISCOVERY_RESPONSE:
                return handleServiceDiscoveryResponse(message);
                
            case MSG_CHANNEL_OPEN_REQUEST:
                return handleChannelOpenRequest(message);
                
            case MSG_CHANNEL_OPEN_RESPONSE:
                return handleChannelOpenResponse(message);
                
            case MSG_PING:
                return handlePing(message);
                
            case MSG_PONG:
                return handlePong(message);
                
            case MSG_NAV_FOCUS_REQUEST:
                return handleNavFocusRequest(message);
                
            case MSG_AUDIO_FOCUS_REQUEST:
                return handleAudioFocusRequest(message);
                
            case MSG_VIDEO_FOCUS_REQUEST:
                return handleVideoFocusRequest(message);
                
            default:
                logger.logWarning("Unknown message type: " + String.format("0x%04X", message.getMessageType()));
                return new ProtocolResponse(false, null);
        }
    }
    
    private ProtocolResponse handleVersionRequest(ProtocolMessage message) {
        if (message.getPayload() != null && message.getPayload().length >= 4) {
            ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            int preferredVersion = buffer.getShort() & 0xFFFF;
            int minimumVersion = buffer.getShort() & 0xFFFF;
            
            logger.logInfo("Version request - Preferred: " + String.format("0x%04X", preferredVersion) + 
                          ", Minimum: " + String.format("0x%04X", minimumVersion));
            
            // Choose the highest version we support
            int chosenVersion = AAP_VERSION_1_0;
            if (preferredVersion >= AAP_VERSION_2_0) {
                chosenVersion = AAP_VERSION_2_0;
            }
            
            byte[] response = createVersionResponse(chosenVersion);
            return new ProtocolResponse(true, response);
        }
        
        return new ProtocolResponse(false, null);
    }
    
    private ProtocolResponse handleVersionResponse(ProtocolMessage message) {
        if (message.getPayload() != null && message.getPayload().length >= 2) {
            ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            int version = buffer.getShort() & 0xFFFF;
            this.protocolVersion = version;
            
            logger.logInfo("Version response received: " + String.format("0x%04X", version));
            return new ProtocolResponse(true, null);
        }
        
        return new ProtocolResponse(false, null);
    }
    
    private ProtocolResponse handleServiceDiscoveryRequest(ProtocolMessage message) {
        // Return available services for degoogled implementation
        String[] availableServices = {
            SERVICE_MEDIA_PLAYBACK,
            SERVICE_INPUT,
            SERVICE_VIDEO,
            SERVICE_NAVIGATION
        };
        
        byte[] response = createServiceDiscoveryResponse(availableServices);
        return new ProtocolResponse(true, response);
    }
    
    private ProtocolResponse handleServiceDiscoveryResponse(ProtocolMessage message) {
        if (message.getPayload() != null) {
            String services = new String(message.getPayload());
            logger.logInfo("Available services: " + services);
            return new ProtocolResponse(true, null);
        }
        
        return new ProtocolResponse(false, null);
    }
    
    private ProtocolResponse handleChannelOpenRequest(ProtocolMessage message) {
        if (message.getPayload() != null && message.getPayload().length >= 4) {
            ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            int channelId = buffer.getInt();
            
            if (buffer.remaining() > 0) {
                byte[] serviceNameBytes = new byte[buffer.remaining()];
                buffer.get(serviceNameBytes);
                String serviceName = new String(serviceNameBytes);
                
                logger.logInfo("Channel open request - ID: " + channelId + ", Service: " + serviceName);
                
                // Accept all channel open requests for now
                byte[] response = createChannelOpenResponse(channelId, true);
                return new ProtocolResponse(true, response);
            }
        }
        
        return new ProtocolResponse(false, null);
    }
    
    private ProtocolResponse handleChannelOpenResponse(ProtocolMessage message) {
        if (message.getPayload() != null && message.getPayload().length >= 5) {
            ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            int channelId = buffer.getInt();
            boolean success = buffer.get() != 0;
            
            logger.logInfo("Channel open response - ID: " + channelId + ", Success: " + success);
            return new ProtocolResponse(true, null);
        }
        
        return new ProtocolResponse(false, null);
    }
    
    private ProtocolResponse handlePing(ProtocolMessage message) {
        if (message.getPayload() != null && message.getPayload().length >= 8) {
            ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            long timestamp = buffer.getLong();
            byte[] response = createPong(timestamp);
            
            return new ProtocolResponse(true, response);
        }
        
        return new ProtocolResponse(false, null);
    }
    
    private ProtocolResponse handlePong(ProtocolMessage message) {
        if (message.getPayload() != null && message.getPayload().length >= 8) {
            ByteBuffer buffer = ByteBuffer.wrap(message.getPayload());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            long timestamp = buffer.getLong();
            long currentTime = System.currentTimeMillis();
            long latency = currentTime - timestamp;
            
            logger.logInfo("Pong received - Latency: " + latency + "ms");
            return new ProtocolResponse(true, null);
        }
        
        return new ProtocolResponse(false, null);
    }
    
    private ProtocolResponse handleNavFocusRequest(ProtocolMessage message) {
        logger.logInfo("Navigation focus requested");
        // Grant navigation focus
        byte[] response = createMessage(MSG_NAV_FOCUS_NOTIFICATION, CHANNEL_ID_CONTROL, 
                                      new byte[]{1}); // 1 = focus granted
        return new ProtocolResponse(true, response);
    }
    
    private ProtocolResponse handleAudioFocusRequest(ProtocolMessage message) {
        logger.logInfo("Audio focus requested");
        // Grant audio focus
        byte[] response = createMessage(MSG_AUDIO_FOCUS_NOTIFICATION, CHANNEL_ID_CONTROL, 
                                      new byte[]{1}); // 1 = focus granted
        return new ProtocolResponse(true, response);
    }
    
    private ProtocolResponse handleVideoFocusRequest(ProtocolMessage message) {
        logger.logInfo("Video focus requested");
        // Grant video focus
        byte[] response = createMessage(MSG_VIDEO_FOCUS_NOTIFICATION, CHANNEL_ID_CONTROL, 
                                      new byte[]{1}); // 1 = focus granted
        return new ProtocolResponse(true, response);
    }
    
    /**
     * Get human-readable message type name
     */
    private String getMessageTypeName(int messageType) {
        switch (messageType) {
            case MSG_VERSION_REQUEST: return "VERSION_REQUEST";
            case MSG_VERSION_RESPONSE: return "VERSION_RESPONSE";
            case MSG_SSL_HANDSHAKE: return "SSL_HANDSHAKE";
            case MSG_AUTH_COMPLETE: return "AUTH_COMPLETE";
            case MSG_SERVICE_DISCOVERY_REQUEST: return "SERVICE_DISCOVERY_REQUEST";
            case MSG_SERVICE_DISCOVERY_RESPONSE: return "SERVICE_DISCOVERY_RESPONSE";
            case MSG_CHANNEL_OPEN_REQUEST: return "CHANNEL_OPEN_REQUEST";
            case MSG_CHANNEL_OPEN_RESPONSE: return "CHANNEL_OPEN_RESPONSE";
            case MSG_PING: return "PING";
            case MSG_PONG: return "PONG";
            case MSG_NAV_FOCUS_REQUEST: return "NAV_FOCUS_REQUEST";
            case MSG_NAV_FOCUS_NOTIFICATION: return "NAV_FOCUS_NOTIFICATION";
            case MSG_SHUTDOWN_REQUEST: return "SHUTDOWN_REQUEST";
            case MSG_SHUTDOWN_RESPONSE: return "SHUTDOWN_RESPONSE";
            case MSG_VOICE_SESSION_REQUEST: return "VOICE_SESSION_REQUEST";
            case MSG_AUDIO_FOCUS_REQUEST: return "AUDIO_FOCUS_REQUEST";
            case MSG_AUDIO_FOCUS_NOTIFICATION: return "AUDIO_FOCUS_NOTIFICATION";
            case MSG_VIDEO_FOCUS_REQUEST: return "VIDEO_FOCUS_REQUEST";
            case MSG_VIDEO_FOCUS_NOTIFICATION: return "VIDEO_FOCUS_NOTIFICATION";
            default: return "UNKNOWN_" + String.format("0x%04X", messageType);
        }
    }
    
    // Getters
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    public boolean isAuthenticated() {
        return isAuthenticated;
    }
    
    /**
     * Protocol message container
     */
    public static class ProtocolMessage {
        private final int messageType;
        private final int channelId;
        private final byte[] payload;
        
        public ProtocolMessage(int messageType, int channelId, byte[] payload) {
            this.messageType = messageType;
            this.channelId = channelId;
            this.payload = payload;
        }
        
        public int getMessageType() { return messageType; }
        public int getChannelId() { return channelId; }
        public byte[] getPayload() { return payload; }
    }
    
    /**
     * Protocol response container
     */
    public static class ProtocolResponse {
        private final boolean success;
        private final byte[] responseMessage;
        
        public ProtocolResponse(boolean success, byte[] responseMessage) {
            this.success = success;
            this.responseMessage = responseMessage;
        }
        
        public boolean isSuccess() { return success; }
        public byte[] getResponseMessage() { return responseMessage; }
    }
}