package com.degoogled.androidauto.protocol.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Base class for all Android Auto protocol messages.
 * Provides common functionality for message serialization and deserialization.
 */
public abstract class Message {
    protected int channelId;
    protected int messageType;
    
    /**
     * Create a new message with the specified channel ID and message type
     */
    public Message(int channelId, int messageType) {
        this.channelId = channelId;
        this.messageType = messageType;
    }
    
    /**
     * Get the channel ID for this message
     */
    public int getChannelId() {
        return channelId;
    }
    
    /**
     * Get the message type for this message
     */
    public int getMessageType() {
        return messageType;
    }
    
    /**
     * Serialize the message to a byte array
     */
    public byte[] serialize() {
        // Get the payload size
        byte[] payload = serializePayload();
        
        // Create a buffer for the full message
        // Header: 1 byte channel ID + 1 byte message type + 2 bytes payload length
        ByteBuffer buffer = ByteBuffer.allocate(4 + payload.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Write the header
        buffer.put((byte) channelId);
        buffer.put((byte) messageType);
        buffer.putShort((short) payload.length);
        
        // Write the payload
        buffer.put(payload);
        
        return buffer.array();
    }
    
    /**
     * Serialize the message payload to a byte array
     */
    protected abstract byte[] serializePayload();
    
    /**
     * Deserialize a message from a byte array
     */
    public static Message deserialize(byte[] data) {
        if (data.length < 4) {
            throw new IllegalArgumentException("Message data too short");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Read the header
        int channelId = buffer.get() & 0xFF;
        int messageType = buffer.get() & 0xFF;
        int payloadLength = buffer.getShort() & 0xFFFF;
        
        // Check that the payload length is valid
        if (data.length < 4 + payloadLength) {
            throw new IllegalArgumentException("Message data too short for payload");
        }
        
        // Extract the payload
        byte[] payload = new byte[payloadLength];
        buffer.get(payload);
        
        // Create the appropriate message type
        return MessageFactory.createMessage(channelId, messageType, payload);
    }
    
    /**
     * Get a string representation of the message
     */
    @Override
    public String toString() {
        return "Message{channelId=" + channelId + ", messageType=" + messageType + "}";
    }
}