package com.degoogled.androidauto.protocol.messages.auth;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Authentication challenge response message.
 * Sent from the phone to the head unit in response to an authentication challenge.
 */
public class AuthChallengeResponse extends Message {
    private byte[] response;
    
    /**
     * Create a new authentication challenge response
     */
    public AuthChallengeResponse(byte[] response) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_CHALLENGE_RESPONSE);
        this.response = response;
    }
    
    /**
     * Create a new authentication challenge response from raw data
     */
    public AuthChallengeResponse(byte[] payload) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_CHALLENGE_RESPONSE);
        deserializePayload(payload);
    }
    
    /**
     * Get the response data
     */
    public byte[] getResponse() {
        return response;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(2 + response.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) response.length);
        buffer.put(response);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 2) {
            throw new IllegalArgumentException("Payload too short for AuthChallengeResponse");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int responseLength = buffer.getShort() & 0xFFFF;
        if (payload.length < 2 + responseLength) {
            throw new IllegalArgumentException("Payload too short for response data");
        }
        
        response = new byte[responseLength];
        buffer.get(response);
    }
    
    @Override
    public String toString() {
        return "AuthChallengeResponse{responseLength=" + 
               (response != null ? response.length : 0) + "}";
    }
}