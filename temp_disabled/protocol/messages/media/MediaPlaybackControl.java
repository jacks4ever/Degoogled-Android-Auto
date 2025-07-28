package com.degoogled.androidauto.protocol.messages.media;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Media playback control message.
 * Sent from the head unit to the phone to control media playback.
 */
public class MediaPlaybackControl extends Message {
    // Playback control commands
    public static final int COMMAND_PLAY = 0x01;
    public static final int COMMAND_PAUSE = 0x02;
    public static final int COMMAND_NEXT = 0x03;
    public static final int COMMAND_PREVIOUS = 0x04;
    public static final int COMMAND_SEEK = 0x05;
    public static final int COMMAND_STOP = 0x06;
    
    private int command;
    private long seekPosition;
    
    /**
     * Create a new media playback control message
     */
    public MediaPlaybackControl(int command, long seekPosition) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_PLAYBACK_CONTROL);
        this.command = command;
        this.seekPosition = seekPosition;
    }
    
    /**
     * Create a new media playback control message from raw data
     */
    public MediaPlaybackControl(byte[] payload) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_PLAYBACK_CONTROL);
        deserializePayload(payload);
    }
    
    /**
     * Get the command
     */
    public int getCommand() {
        return command;
    }
    
    /**
     * Get the seek position in milliseconds
     */
    public long getSeekPosition() {
        return seekPosition;
    }
    
    @Override
    protected byte[] serializePayload() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put((byte) command);
        buffer.put((byte) 0); // Reserved
        buffer.putLong(seekPosition);
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 2) {
            throw new IllegalArgumentException("Payload too short for MediaPlaybackControl");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        command = buffer.get() & 0xFF;
        buffer.get(); // Skip reserved byte
        
        // If this is a seek command, read the seek position
        if (command == COMMAND_SEEK && payload.length >= 10) {
            seekPosition = buffer.getLong();
        } else {
            seekPosition = 0;
        }
    }
    
    @Override
    public String toString() {
        return "MediaPlaybackControl{command=" + command + 
               ", seekPosition=" + seekPosition + "}";
    }
}