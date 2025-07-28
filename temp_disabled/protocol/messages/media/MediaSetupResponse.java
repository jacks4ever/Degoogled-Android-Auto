package com.degoogled.androidauto.protocol.messages.media;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Media channel setup response message.
 * Sent from the head unit to the phone in response to a media setup request.
 */
public class MediaSetupResponse extends Message {
    private int status;
    private int protocolVersion;
    private int maxProtocolVersion;
    private int config;
    
    /**
     * Create a new media setup response
     */
    public MediaSetupResponse(int status, int protocolVersion, int maxProtocolVersion, int config) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_SETUP_RESPONSE);
        this.status = status;
        this.protocolVersion = protocolVersion;
        this.maxProtocolVersion = maxProtocolVersion;
        this.config = config;
    }
    
    /**
     * Create a new media setup response from raw data
     */
    public MediaSetupResponse(byte[] payload) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_SETUP_RESPONSE);
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
     * Get the configuration flags
     */
    public int getConfig() {
        return config;
    }
    
    /**
     * Check if the media setup was successful
     */
    public boolean isSuccess() {
        return status == MessageTypes.STATUS_OK;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put((byte) status);
        buffer.put((byte) 0); // Reserved
        buffer.putShort((short) protocolVersion);
        buffer.putShort((short) maxProtocolVersion);
        buffer.putShort((short) config);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 8) {
            throw new IllegalArgumentException("Payload too short for MediaSetupResponse");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        status = buffer.get() & 0xFF;
        buffer.get(); // Skip reserved byte
        protocolVersion = buffer.getShort() & 0xFFFF;
        maxProtocolVersion = buffer.getShort() & 0xFFFF;
        config = buffer.getShort() & 0xFFFF;
    }
    
    @Override
    public String toString() {
        return "MediaSetupResponse{status=" + status + 
               ", protocolVersion=" + protocolVersion + 
               ", maxProtocolVersion=" + maxProtocolVersion + 
               ", config=" + config + "}";
    }
}