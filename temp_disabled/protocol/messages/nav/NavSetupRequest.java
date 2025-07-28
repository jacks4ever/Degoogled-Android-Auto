package com.degoogled.androidauto.protocol.messages.nav;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Navigation channel setup request message.
 * Sent from the phone to the head unit to set up the navigation channel.
 */
public class NavSetupRequest extends Message {
    private int protocolVersion;
    private int maxProtocolVersion;
    private int config;
    
    /**
     * Create a new navigation setup request
     */
    public NavSetupRequest(int protocolVersion, int maxProtocolVersion, int config) {
        super(MessageTypes.CHANNEL_NAVIGATION, MessageTypes.NAV_MSG_SETUP_REQUEST);
        this.protocolVersion = protocolVersion;
        this.maxProtocolVersion = maxProtocolVersion;
        this.config = config;
    }
    
    /**
     * Create a new navigation setup request from raw data
     */
    public NavSetupRequest(byte[] payload) {
        super(MessageTypes.CHANNEL_NAVIGATION, MessageTypes.NAV_MSG_SETUP_REQUEST);
        deserializePayload(payload);
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
     * Get the configuration flags
     */
    public int getConfig() {
        return config;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) protocolVersion);
        buffer.putShort((short) maxProtocolVersion);
        buffer.putShort((short) config);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 6) {
            throw new IllegalArgumentException("Payload too short for NavSetupRequest");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        protocolVersion = buffer.getShort() & 0xFFFF;
        maxProtocolVersion = buffer.getShort() & 0xFFFF;
        config = buffer.getShort() & 0xFFFF;
    }
    
    @Override
    public String toString() {
        return "NavSetupRequest{protocolVersion=" + protocolVersion + 
               ", maxProtocolVersion=" + maxProtocolVersion + 
               ", config=" + config + "}";
    }
}