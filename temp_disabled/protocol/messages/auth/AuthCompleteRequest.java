package com.degoogled.androidauto.protocol.messages.auth;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Authentication complete request message.
 * Sent from the phone to the head unit to complete the authentication process.
 */
public class AuthCompleteRequest extends Message {
    private int status;
    
    /**
     * Create a new authentication complete request
     */
    public AuthCompleteRequest(int status) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_COMPLETE_REQUEST);
        this.status = status;
    }
    
    /**
     * Create a new authentication complete request from raw data
     */
    public AuthCompleteRequest(byte[] payload) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_COMPLETE_REQUEST);
        deserializePayload(payload);
    }
    
    /**
     * Get the status code
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Check if the authentication was successful
     */
    public boolean isSuccess() {
        return status == MessageTypes.STATUS_OK;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put((byte) status);
        buffer.put((byte) 0); // Reserved
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 1) {
            throw new IllegalArgumentException("Payload too short for AuthCompleteRequest");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        status = buffer.get() & 0xFF;
    }
    
    @Override
    public String toString() {
        return "AuthCompleteRequest{status=" + status + "}";
    }
}