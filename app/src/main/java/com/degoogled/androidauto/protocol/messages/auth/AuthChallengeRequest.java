package com.degoogled.androidauto.protocol.messages.auth;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Authentication challenge request message.
 * Sent from the head unit to the phone to challenge the authentication.
 */
public class AuthChallengeRequest extends Message {
    private byte[] challenge;
    
    /**
     * Create a new authentication challenge request
     */
    public AuthChallengeRequest(byte[] challenge) {
        super(MessageTypes.CHANNEL_AUTH, MessageTypes.AUTH_MSG_CHALLENGE_REQUEST);
        this.challenge = challenge;
    }
    
    /**
     * Create a new authentication challenge request from raw payload data
     */
    public static AuthChallengeRequest fromPayload(byte[] payload) {
        AuthChallengeRequest request = new AuthChallengeRequest(new byte[0]);
        request.deserializePayload(payload);
        return request;
    }
    
    /**
     * Get the challenge data
     */
    public byte[] getChallenge() {
        return challenge;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(2 + challenge.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) challenge.length);
        buffer.put(challenge);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 2) {
            throw new IllegalArgumentException("Payload too short for AuthChallengeRequest");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int challengeLength = buffer.getShort() & 0xFFFF;
        if (payload.length < 2 + challengeLength) {
            throw new IllegalArgumentException("Payload too short for challenge data");
        }
        
        challenge = new byte[challengeLength];
        buffer.get(challenge);
    }
    
    @Override
    public String toString() {
        return "AuthChallengeRequest{challengeLength=" + 
               (challenge != null ? challenge.length : 0) + "}";
    }
}