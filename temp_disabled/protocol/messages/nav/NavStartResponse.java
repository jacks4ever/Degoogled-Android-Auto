package com.degoogled.androidauto.protocol.messages.nav;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Navigation start response message.
 * Sent from the head unit to the phone in response to a navigation start request.
 */
public class NavStartResponse extends Message {
    private int status;
    private int config;
    
    /**
     * Create a new navigation start response
     */
    public NavStartResponse(int status, int config) {
        super(MessageTypes.CHANNEL_NAVIGATION, MessageTypes.NAV_MSG_START_RESPONSE);
        this.status = status;
        this.config = config;
    }
    
    /**
     * Create a new navigation start response from raw data
     */
    public NavStartResponse(byte[] payload) {
        super(MessageTypes.CHANNEL_NAVIGATION, MessageTypes.NAV_MSG_START_RESPONSE);
        deserializePayload(payload);
    }
    
    /**
     * Get the status code
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Get the configuration flags
     */
    public int getConfig() {
        return config;
    }
    
    /**
     * Check if the navigation start was successful
     */
    public boolean isSuccess() {
        return status == MessageTypes.STATUS_OK;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put((byte) status);
        buffer.put((byte) 0); // Reserved
        buffer.putShort((short) config);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 4) {
            throw new IllegalArgumentException("Payload too short for NavStartResponse");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        status = buffer.get() & 0xFF;
        buffer.get(); // Skip reserved byte
        config = buffer.getShort() & 0xFFFF;
    }
    
    @Override
    public String toString() {
        return "NavStartResponse{status=" + status + 
               ", config=" + config + "}";
    }
}