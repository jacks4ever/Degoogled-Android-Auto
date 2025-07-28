package com.degoogled.androidauto.protocol.messages.control;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Service discovery response message.
 * Sent from the head unit to the phone in response to a service discovery request.
 */
public class ServiceDiscoveryResponse extends Message {
    private int status;
    private List<Service> services;
    
    /**
     * Create a new service discovery response
     */
    public ServiceDiscoveryResponse(int status, List<Service> services) {
        super(MessageTypes.CHANNEL_CONTROL, MessageTypes.CONTROL_MSG_SERVICE_DISCOVERY_RESPONSE);
        this.status = status;
        this.services = services;
    }
    
    /**
     * Create a new service discovery response from raw data
     */
    public ServiceDiscoveryResponse(byte[] payload) {
        super(MessageTypes.CHANNEL_CONTROL, MessageTypes.CONTROL_MSG_SERVICE_DISCOVERY_RESPONSE);
        deserializePayload(payload);
    }
    
    /**
     * Get the status code
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Get the list of available services
     */
    public List<Service> getServices() {
        return services;
    }
    
    /**
     * Check if the service discovery was successful
     */
    public boolean isSuccess() {
        return status == MessageTypes.STATUS_OK;
    }
    
    @Override
    protected byte[] serializePayload() {
        // Calculate the size of the payload
        int size = 4; // status (1) + reserved (1) + service count (2)
        for (Service service : services) {
            size += 4; // service ID (2) + channel ID (1) + reserved (1)
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put((byte) status);
        buffer.put((byte) 0); // Reserved
        buffer.putShort((short) services.size());
        
        for (Service service : services) {
            buffer.putShort((short) service.serviceId);
            buffer.put((byte) service.channelId);
            buffer.put((byte) 0); // Reserved
        }
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 4) {
            throw new IllegalArgumentException("Payload too short for ServiceDiscoveryResponse");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        status = buffer.get() & 0xFF;
        buffer.get(); // Skip reserved byte
        int serviceCount = buffer.getShort() & 0xFFFF;
        
        services = new ArrayList<>(serviceCount);
        
        for (int i = 0; i < serviceCount; i++) {
            if (buffer.remaining() < 4) {
                throw new IllegalArgumentException("Payload too short for service data");
            }
            
            int serviceId = buffer.getShort() & 0xFFFF;
            int channelId = buffer.get() & 0xFF;
            buffer.get(); // Skip reserved byte
            
            services.add(new Service(serviceId, channelId));
        }
    }
    
    @Override
    public String toString() {
        return "ServiceDiscoveryResponse{status=" + status + 
               ", serviceCount=" + (services != null ? services.size() : 0) + "}";
    }
    
    /**
     * Represents a service available on the head unit
     */
    public static class Service {
        private int serviceId;
        private int channelId;
        
        /**
         * Create a new service
         */
        public Service(int serviceId, int channelId) {
            this.serviceId = serviceId;
            this.channelId = channelId;
        }
        
        /**
         * Get the service ID
         */
        public int getServiceId() {
            return serviceId;
        }
        
        /**
         * Get the channel ID
         */
        public int getChannelId() {
            return channelId;
        }
        
        @Override
        public String toString() {
            return "Service{serviceId=" + serviceId + ", channelId=" + channelId + "}";
        }
    }
}