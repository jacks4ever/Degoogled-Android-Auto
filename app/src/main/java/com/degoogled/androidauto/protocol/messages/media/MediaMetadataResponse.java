package com.degoogled.androidauto.protocol.messages.media;

import com.degoogled.androidauto.protocol.messages.Message;
import com.degoogled.androidauto.protocol.messages.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Media metadata response message.
 * Sent from the phone to the head unit in response to a media metadata request.
 */
public class MediaMetadataResponse extends Message {
    private int status;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private byte[] albumArt;
    
    /**
     * Create a new media metadata response
     */
    public MediaMetadataResponse(int status, String title, String artist, String album, long duration, byte[] albumArt) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_METADATA_RESPONSE);
        this.status = status;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.albumArt = albumArt;
    }
    
    /**
     * Create a new media metadata response from raw data
     */
    public MediaMetadataResponse(byte[] payload) {
        super(MessageTypes.CHANNEL_MEDIA, MessageTypes.MEDIA_MSG_METADATA_RESPONSE);
        deserializePayload(payload);
    }
    
    /**
     * Get the status code
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Get the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Get the artist
     */
    public String getArtist() {
        return artist;
    }
    
    /**
     * Get the album
     */
    public String getAlbum() {
        return album;
    }
    
    /**
     * Get the duration in milliseconds
     */
    public long getDuration() {
        return duration;
    }
    
    /**
     * Get the album art data
     */
    public byte[] getAlbumArt() {
        return albumArt;
    }
    
    /**
     * Check if the metadata request was successful
     */
    public boolean isSuccess() {
        return status == MessageTypes.STATUS_OK;
    }
    
    @Override
    protected byte[] serializePayload() {
        byte[] titleBytes = title != null ? title.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] artistBytes = artist != null ? artist.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] albumBytes = album != null ? album.getBytes(StandardCharsets.UTF_8) : new byte[0];
        
        // Calculate the size of the payload
        int size = 19; // status (1) + reserved (1) + title length (2) + artist length (2) + album length (2) + duration (8) + album art length (2) + reserved (1)
        size += titleBytes.length + artistBytes.length + albumBytes.length;
        if (albumArt != null) {
            size += albumArt.length;
        }
        
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put((byte) status);
        buffer.put((byte) 0); // Reserved
        
        // Write the title
        buffer.putShort((short) titleBytes.length);
        buffer.put(titleBytes);
        
        // Write the artist
        buffer.putShort((short) artistBytes.length);
        buffer.put(artistBytes);
        
        // Write the album
        buffer.putShort((short) albumBytes.length);
        buffer.put(albumBytes);
        
        // Write the duration
        buffer.putLong(duration);
        
        // Write the album art
        if (albumArt != null) {
            buffer.putShort((short) albumArt.length);
            buffer.put(albumArt);
        } else {
            buffer.putShort((short) 0);
        }
        
        buffer.put((byte) 0); // Reserved
        
        return buffer.array();
    }
    
    /**
     * Deserialize the payload data
     */
    private void deserializePayload(byte[] payload) {
        if (payload.length < 19) {
            throw new IllegalArgumentException("Payload too short for MediaMetadataResponse");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        status = buffer.get() & 0xFF;
        buffer.get(); // Skip reserved byte
        
        // Read the title
        int titleLength = buffer.getShort() & 0xFFFF;
        if (buffer.remaining() < titleLength) {
            throw new IllegalArgumentException("Payload too short for title data");
        }
        byte[] titleBytes = new byte[titleLength];
        buffer.get(titleBytes);
        title = new String(titleBytes, StandardCharsets.UTF_8);
        
        // Read the artist
        int artistLength = buffer.getShort() & 0xFFFF;
        if (buffer.remaining() < artistLength) {
            throw new IllegalArgumentException("Payload too short for artist data");
        }
        byte[] artistBytes = new byte[artistLength];
        buffer.get(artistBytes);
        artist = new String(artistBytes, StandardCharsets.UTF_8);
        
        // Read the album
        int albumLength = buffer.getShort() & 0xFFFF;
        if (buffer.remaining() < albumLength) {
            throw new IllegalArgumentException("Payload too short for album data");
        }
        byte[] albumBytes = new byte[albumLength];
        buffer.get(albumBytes);
        album = new String(albumBytes, StandardCharsets.UTF_8);
        
        // Read the duration
        duration = buffer.getLong();
        
        // Read the album art
        int albumArtLength = buffer.getShort() & 0xFFFF;
        if (albumArtLength > 0) {
            if (buffer.remaining() < albumArtLength) {
                throw new IllegalArgumentException("Payload too short for album art data");
            }
            albumArt = new byte[albumArtLength];
            buffer.get(albumArt);
        } else {
            albumArt = null;
        }
    }
    
    @Override
    public String toString() {
        return "MediaMetadataResponse{status=" + status + 
               ", title='" + title + "'" + 
               ", artist='" + artist + "'" + 
               ", album='" + album + "'" + 
               ", duration=" + duration + 
               ", albumArtLength=" + (albumArt != null ? albumArt.length : 0) + "}";
    }
}