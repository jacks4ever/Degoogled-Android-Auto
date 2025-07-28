package com.degoogled.androidauto.protocol.messages.media;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Media channel setup request message.
 * Sent from the phone to the head unit to set up the media channel.
 */
public class MediaSetupRequest extends Message {
    private int protocolVersion;
    private int maxProtocolVersion;
    private int config;
    
    /**
     * Create a new media setup request
     */
    public MediaSetupRequest(int protocolVersion, int maxProtocolVersion, int config) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_SETUP_REQUEST);
        this.protocolVersion = protocolVersion;
        this.maxProtocolVersion = maxProtocolVersion;
        this.config = config;
    }
    
    /**
     * Create a new media setup request from raw data
     */
    public MediaSetupRequest(byte[] payload) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_SETUP_REQUEST);
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
            throw new IllegalArgumentException("Payload too short for MediaSetupRequest");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        protocolVersion = buffer.getShort() & 0xFFFF;
        maxProtocolVersion = buffer.getShort() & 0xFFFF;
        config = buffer.getShort() & 0xFFFF;
    }
    
    @Override
    public String toString() {
        return "MediaSetupRequest{protocolVersion=" + protocolVersion + 
               ", maxProtocolVersion=" + maxProtocolVersion + 
               ", config=" + config + "}";
    }
}