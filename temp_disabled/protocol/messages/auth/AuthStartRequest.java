package com.degoogled.androidauto.protocol.messages.auth;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Authentication start request message.
 * Sent from the phone to the head unit to initiate the authentication process.
 */
public class AuthStartRequest extends Message {
    private int protocolVersion;
    private int authMethod;
    private long deviceId;
    
    /**
     * Create a new authentication start request
     */
    public AuthStartRequest(int protocolVersion, int authMethod, long deviceId) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_START_REQUEST);
        this.protocolVersion = protocolVersion;
        this.authMethod = authMethod;
        this.deviceId = deviceId;
    }
    
    /**
     * Create a new authentication start request from raw data
     */
    public AuthStartRequest(byte[] payload) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_START_REQUEST);
        deserializePayload(payload);
    }
    
    /**
     * Get the protocol version
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    /**
     * Get the authentication method
     */
    public int getAuthMethod() {
        return authMethod;
    }
    
    /**
     * Get the device ID
     */
    public long getDeviceId() {
        return deviceId;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) protocolVersion);
        buffer.put((byte) authMethod);
        buffer.put((byte) 0); // Reserved
        buffer.putLong(deviceId);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 10) {
            throw new IllegalArgumentException("Payload too short for AuthStartRequest");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        protocolVersion = buffer.getShort() & 0xFFFF;
        authMethod = buffer.get() & 0xFF;
        buffer.get(); // Skip reserved byte
        deviceId = buffer.getLong();
    }
    
    @Override
    public String toString() {
        return "AuthStartRequest{protocolVersion=" + protocolVersion + 
               ", authMethod=" + authMethod + 
               ", deviceId=" + deviceId + "}";
    }
}