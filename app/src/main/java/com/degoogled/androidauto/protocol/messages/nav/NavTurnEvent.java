package com.degoogled.androidauto.protocol.messages.nav;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Navigation turn event message.
 * Sent from the phone to the head unit to provide turn-by-turn navigation instructions.
 */
public class NavTurnEvent extends Message {
    // Turn types
    public static final int TURN_TYPE_STRAIGHT = 0x01;
    public static final int TURN_TYPE_RIGHT = 0x02;
    public static final int TURN_TYPE_LEFT = 0x03;
    public static final int TURN_TYPE_SLIGHT_RIGHT = 0x04;
    public static final int TURN_TYPE_SLIGHT_LEFT = 0x05;
    public static final int TURN_TYPE_SHARP_RIGHT = 0x06;
    public static final int TURN_TYPE_SHARP_LEFT = 0x07;
    public static final int TURN_TYPE_U_TURN = 0x08;
    public static final int TURN_TYPE_ROUNDABOUT = 0x09;
    public static final int TURN_TYPE_EXIT = 0x0A;
    public static final int TURN_TYPE_DESTINATION = 0x0B;
    
    private int turnType;
    private int distance;  // Distance in meters
    private String instruction;
    private String roadName;
    private byte[] turnIcon;
    
    /**
     * Create a new navigation turn event
     */
    public NavTurnEvent(int turnType, int distance, String instruction, String roadName, byte[] turnIcon) {
        super(MessageTypes.CHANNEL_NAVIGATION, MessageTypes.NAV_MSG_TURN_EVENT);
        this.turnType = turnType;
        this.distance = distance;
        this.instruction = instruction;
        this.roadName = roadName;
        this.turnIcon = turnIcon;
    }
    
    /**
     * Create a new navigation turn event from raw data
     */
    public NavTurnEvent(byte[] payload) {
        super(MessageTypes.CHANNEL_NAVIGATION, MessageTypes.NAV_MSG_TURN_EVENT);
        deserializePayload(payload);
    }
    
    /**
     * Get the turn type
     */
    public int getTurnType() {
        return turnType;
    }
    
    /**
     * Get the distance to the turn in meters
     */
    public int getDistance() {
        return distance;
    }
    
    /**
     * Get the turn instruction
     */
    public String getInstruction() {
        return instruction;
    }
    
    /**
     * Get the road name
     */
    public String getRoadName() {
        return roadName;
    }
    
    /**
     * Get the turn icon data
     */
    public byte[] getTurnIcon() {
        return turnIcon;
    }
    
    @Override
    protected byte[] serializePayload() {
        byte[] instructionBytes = instruction != null ? instruction.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] roadNameBytes = roadName != null ? roadName.getBytes(StandardCharsets.UTF_8) : new byte[0];
        
        // Calculate the size of the payload
        int size = 10; // turn type (1) + reserved (1) + distance (4) + instruction length (2) + road name length (2)
        size += instructionBytes.length + roadNameBytes.length;
        if (turnIcon != null) {
            size += 2 + turnIcon.length; // turn icon length (2) + turn icon data
        } else {
            size += 2; // turn icon length (2)
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put((byte) turnType);
        buffer.put((byte) 0); // Reserved
        buffer.putInt(distance);
        
        // Write the instruction
        buffer.putShort((short) instructionBytes.length);
        buffer.put(instructionBytes);
        
        // Write the road name
        buffer.putShort((short) roadNameBytes.length);
        buffer.put(roadNameBytes);
        
        // Write the turn icon
        if (turnIcon != null) {
            buffer.putShort((short) turnIcon.length);
            buffer.put(turnIcon);
        } else {
            buffer.putShort((short) 0);
        }
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 10) {
            throw new IllegalArgumentException("Payload too short for NavTurnEvent");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        turnType = buffer.get() & 0xFF;
        buffer.get(); // Skip reserved byte
        distance = buffer.getInt();
        
        // Read the instruction
        int instructionLength = buffer.getShort() & 0xFFFF;
        if (buffer.remaining() < instructionLength) {
            throw new IllegalArgumentException("Payload too short for instruction data");
        }
        byte[] instructionBytes = new byte[instructionLength];
        buffer.get(instructionBytes);
        instruction = new String(instructionBytes, StandardCharsets.UTF_8);
        
        // Read the road name
        int roadNameLength = buffer.getShort() & 0xFFFF;
        if (buffer.remaining() < roadNameLength) {
            throw new IllegalArgumentException("Payload too short for road name data");
        }
        byte[] roadNameBytes = new byte[roadNameLength];
        buffer.get(roadNameBytes);
        roadName = new String(roadNameBytes, StandardCharsets.UTF_8);
        
        // Read the turn icon
        int turnIconLength = buffer.getShort() & 0xFFFF;
        if (turnIconLength > 0) {
            if (buffer.remaining() < turnIconLength) {
                throw new IllegalArgumentException("Payload too short for turn icon data");
            }
            turnIcon = new byte[turnIconLength];
            buffer.get(turnIcon);
        } else {
            turnIcon = null;
        }
    }
    
    @Override
    public String toString() {
        return "NavTurnEvent{turnType=" + turnType + 
               ", distance=" + distance + 
               ", instruction='" + instruction + "'" + 
               ", roadName='" + roadName + "'" + 
               ", turnIconLength=" + (turnIcon != null ? turnIcon.length : 0) + "}";
    }
}