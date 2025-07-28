package com.degoogled.androidauto.protocol.messages.media;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Media metadata request message.
 * Sent from the head unit to the phone to request media metadata.
 */
public class MediaMetadataRequest extends Message {
    
    /**
     * Create a new media metadata request
     */
    public MediaMetadataRequest() {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_METADATA_REQUEST);
    }
    
    /**
     * Create a new media metadata request from raw data
     */
    public MediaMetadataRequest(byte[] payload) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_METADATA_REQUEST);
        // No payload data to deserialize
    }
    
    @Override
    protected byte[] serializePayload() {
        // No payload data
        return new byte[0];
    }
    
    @Override
    public String toString() {
        return "MediaMetadataRequest{}";
    }
}