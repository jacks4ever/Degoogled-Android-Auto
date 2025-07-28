package com.degoogled.androidauto.protocol.messages;

import java.util.Arrays;

/**
 * Represents an unknown or unimplemented message type.
 * Used as a fallback when a message cannot be parsed into a specific type.
 */
public class UnknownMessage extends Message {
    private final byte[] payload;
    
    /**
     * Create a new unknown message
     */
    public UnknownMessage(int channelId, int messageType, byte[] payload) {
        super(channelId, messageType);
        this.payload = payload;
    }
    
    /**
     * Get the raw payload of this message
     */
    public byte[] getPayload() {
        return payload;
    }
    
    @Override
    protected byte[] serializePayload() {
        return payload;
    }
    
    @Override
    public String toString() {
        return "UnknownMessage{channelId=" + channelId + 
               ", messageType=" + messageType + 
               ", payloadLength=" + (payload != null ? payload.length : 0) + "}";
    }
}