package com.degoogled.androidauto.protocol.messages.auth;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Authentication start response message.
 * Sent from the head unit to the phone in response to an authentication start request.
 */
public class AuthStartResponse extends Message {
    private int status;
    private int protocolVersion;
    private int maxProtocolVersion;
    
    /**
     * Create a new authentication start response
     */
    public AuthStartResponse(int status, int protocolVersion, int maxProtocolVersion) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_START_RESPONSE);
        this.status = status;
        this.protocolVersion = protocolVersion;
        this.maxProtocolVersion = maxProtocolVersion;
    }
    
    /**
     * Create a new authentication start response from raw data
     */
    public AuthStartResponse(byte[] payload) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_START_RESPONSE);
        deserializePayload(payload);
    }
    
    /**
     * Get the status code
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Get the protocol version
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    /**
     * Get the maximum supported protocol version
     */
    public int getMaxProtocolVersion() {
        return maxProtocolVersion;
    }
    
    /**
     * Check if the authentication start was successful
     */
    public boolean isSuccess() {
        return status == MessageTypes.STATUS_OK;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put((byte) status);
        buffer.put((byte) 0); // Reserved
        buffer.putShort((short) protocolVersion);
        buffer.putShort((short) maxProtocolVersion);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 6) {
            throw new IllegalArgumentException("Payload too short for AuthStartResponse");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        status = buffer.get() & 0xFF;
        buffer.get(); // Skip reserved byte
        protocolVersion = buffer.getShort() & 0xFFFF;
        maxProtocolVersion = buffer.getShort() & 0xFFFF;
    }
    
    @Override
    public String toString() {
        return "AuthStartResponse{status=" + status + 
               ", protocolVersion=" + protocolVersion + 
               ", maxProtocolVersion=" + maxProtocolVersion + "}";
    }
}