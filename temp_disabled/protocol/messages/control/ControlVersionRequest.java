package com.degoogled.androidauto.protocol.messages.control;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Control channel version request message.
 * Sent from the phone to the head unit to negotiate protocol version.
 */
public class ControlVersionRequest extends Message {
    private int protocolVersion;
    private int maxProtocolVersion;
    
    /**
     * Create a new control version request
     */
    public ControlVersionRequest(int protocolVersion, int maxProtocolVersion) {
        super(MessageTypes.CHANNEL_CONTROL, MessageTypes.CONTROL_MSG_VERSION_REQUEST);
        this.protocolVersion = protocolVersion;
        this.maxProtocolVersion = maxProtocolVersion;
    }
    
    /**
     * Create a new control version request from raw data
     */
    public ControlVersionRequest(byte[] payload) {
        super(MessageTypes.CHANNEL_CONTROL, MessageTypes.CONTROL_MSG_VERSION_REQUEST);
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
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.putShort((short) protocolVersion);
        buffer.putShort((short) maxProtocolVersion);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 4) {
            throw new IllegalArgumentException("Payload too short for ControlVersionRequest");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        protocolVersion = buffer.getShort() & 0xFFFF;
        maxProtocolVersion = buffer.getShort() & 0xFFFF;
    }
    
    @Override
    public String toString() {
        return "ControlVersionRequest{protocolVersion=" + protocolVersion + 
               ", maxProtocolVersion=" + maxProtocolVersion + "}";
    }
}