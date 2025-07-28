package com.degoogled.androidauto.protocol.messages.control;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service discovery request message.
 * Sent from the phone to the head unit to discover available services.
 */
public class ServiceDiscoveryRequest extends Message {
    
    /**
     * Create a new service discovery request
     */
    public ServiceDiscoveryRequest() {
        super(MessageTypes.CHANNEL_CONTROL, MessageTypes.CONTROL_MSG_SERVICE_DISCOVERY_REQUEST);
    }
    
    /**
     * Create a new service discovery request from raw data
     */
    public ServiceDiscoveryRequest(byte[] payload) {
        super(MessageTypes.CHANNEL_CONTROL, MessageTypes.CONTROL_MSG_SERVICE_DISCOVERY_REQUEST);
        // No payload data to deserialize
    }
    
    @Override
    protected byte[] serializePayload() {
        // No payload data
        return new byte[0];
    }
    
    @Override
    public String toString() {
        return "ServiceDiscoveryRequest{}";
    }
}