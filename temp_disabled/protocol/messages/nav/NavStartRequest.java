package com.degoogled.androidauto.protocol.messages.nav;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Navigation start request message.
 * Sent from the phone to the head unit to start navigation.
 */
public class NavStartRequest extends Message {
    private int config;
    
    /**
     * Create a new navigation start request
     */
    public NavStartRequest(int config) {
        super(MessageTypes.CHANNEL_NAVIGATION, MessageTypes.NAV_MSG_START_REQUEST);
        this.config = config;
    }
    
    /**
     * Create a new navigation start request from raw data
     */
    public NavStartRequest(byte[] payload) {
        super(MessageTypes.CHANNEL_NAVIGATION, MessageTypes.NAV_MSG_START_REQUEST);
        deserializePayload(payload);
    }
    
    /**
     * Get the configuration flags
     */
    public int getConfig() {
        return config;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) config);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 2) {
            throw new IllegalArgumentException("Payload too short for NavStartRequest");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        config = buffer.getShort() & 0xFFFF;
    }
    
    @Override
    public String toString() {
        return "NavStartRequest{config=" + config + "}";
    }
}