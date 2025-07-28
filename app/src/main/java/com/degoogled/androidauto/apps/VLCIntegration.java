package com.degoogled.androidauto.apps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.degoogled.androidauto.logging.ConnectionLogger;

/**
 * VLC Media Player Integration for Degoogled Android Auto
 * Provides media playback services using VLC
 */
public class VLCIntegration {
    private static final String TAG = "VLCIntegration";
    
    // VLC package names
    private static final String[] VLC_PACKAGES = {
        "org.videolan.vlc",          // VLC for Android
        "org.videolan.vlc.debug",    // VLC debug version
        "org.videolan.vlc.beta"      // VLC beta version
    };
    
    // VLC intents and actions
    private static final String VLC_PLAY_ACTION = "org.videolan.vlc.player.play";
    private static final String VLC_PAUSE_ACTION = "org.videolan.vlc.player.pause";
    private static final String VLC_STOP_ACTION = "org.videolan.vlc.player.stop";
    private static final String VLC_NEXT_ACTION = "org.videolan.vlc.player.next";
    private static final String VLC_PREVIOUS_ACTION = "org.videolan.vlc.player.previous";
    
    // Media session actions
    private static final String ACTION_PLAY = "android.media.action.MEDIA_PLAY";
    private static final String ACTION_PAUSE = "android.media.action.MEDIA_PAUSE";
    private static final String ACTION_STOP = "android.media.action.MEDIA_STOP";
    private static final String ACTION_NEXT = "android.media.action.MEDIA_NEXT";
    private static final String ACTION_PREVIOUS = "android.media.action.MEDIA_PREVIOUS";
    
    private Context context;
    private ConnectionLogger logger;
    private String installedVLCPackage;
    private MediaListener mediaListener;
    private AudioManager audioManager;
    
    // Media state
    private boolean isPlaying = false;
    private String currentTrack;
    private String currentArtist;
    private String currentAlbum;
    private int currentPosition = 0;
    private int totalDuration = 0;
    
    public interface MediaListener {
        void onPlaybackStarted(MediaInfo info);
        void onPlaybackPaused();
        void onPlaybackStopped();
        void onTrackChanged(MediaInfo info);
        void onPositionChanged(int position, int duration);
        void onMediaError(String error);
    }
    
    public static class MediaInfo {
        public final String title;
        public final String artist;
        public final String album;
        public final String artworkUrl;
        public final int duration; // seconds
        public final String source; // file path or URL
        
        public MediaInfo(String title, String artist, String album, 
                        String artworkUrl, int duration, String source) {
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.artworkUrl = artworkUrl;
            this.duration = duration;
            this.source = source;
        }
    }
    
    public VLCIntegration(Context context, ConnectionLogger logger) {
        this.context = context;
        this.logger = logger;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.installedVLCPackage = findInstalledVLC();
    }
    
    public void setMediaListener(MediaListener listener) {
        this.mediaListener = listener;
    }
    
    /**
     * Find installed VLC package
     */
    private String findInstalledVLC() {
        PackageManager pm = context.getPackageManager();
        
        for (String packageName : VLC_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                logger.logInfo("Found VLC package: " + packageName);
                return packageName;
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found, try next
            }
        }
        
        logger.logWarning("No VLC package found");
        return null;
    }
    
    /**
     * Check if VLC is available
     */
    public boolean isVLCAvailable() {
        return installedVLCPackage != null;
    }
    
    /**
     * Play media file
     */
    public boolean playMedia(String filePath) {
        if (!isVLCAvailable()) {
            logger.logError("VLC not available for media playback");
            if (mediaListener != null) {
                mediaListener.onMediaError("VLC not installed");
            }
            return false;
        }
        
        logger.logInfo("Playing media: " + filePath);
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage(installedVLCPackage);
            
            Uri mediaUri = Uri.parse(filePath);
            intent.setDataAndType(mediaUri, "video/*"); // VLC handles both audio and video
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            context.startActivity(intent);
            
            // Update state
            isPlaying = true;
            currentTrack = extractFileName(filePath);
            
            if (mediaListener != null) {
                MediaInfo info = new MediaInfo(currentTrack, "Unknown", "Unknown", null, 0, filePath);
                mediaListener.onPlaybackStarted(info);
            }
            
            logger.logInfo("Media playback started");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to play media: " + e.getMessage());
            if (mediaListener != null) {
                mediaListener.onMediaError("Failed to play media: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Play media from URL
     */
    public boolean playMediaFromUrl(String url) {
        if (!isVLCAvailable()) {
            logger.logError("VLC not available for media playback");
            if (mediaListener != null) {
                mediaListener.onMediaError("VLC not installed");
            }
            return false;
        }
        
        logger.logInfo("Playing media from URL: " + url);
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage(installedVLCPackage);
            
            Uri mediaUri = Uri.parse(url);
            intent.setData(mediaUri);
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(intent);
            
            // Update state
            isPlaying = true;
            currentTrack = extractFileName(url);
            
            if (mediaListener != null) {
                MediaInfo info = new MediaInfo(currentTrack, "Unknown", "Unknown", null, 0, url);
                mediaListener.onPlaybackStarted(info);
            }
            
            logger.logInfo("URL media playback started");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to play media from URL: " + e.getMessage());
            if (mediaListener != null) {
                mediaListener.onMediaError("Failed to play media: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Send play command
     */
    public boolean play() {
        if (!isVLCAvailable()) {
            return false;
        }
        
        logger.logInfo("Sending play command");
        
        try {
            // Try VLC-specific action first
            Intent intent = new Intent(VLC_PLAY_ACTION);
            intent.setPackage(installedVLCPackage);
            context.sendBroadcast(intent);
            
            // Also try standard media action
            Intent mediaIntent = new Intent(ACTION_PLAY);
            mediaIntent.setPackage(installedVLCPackage);
            context.sendBroadcast(mediaIntent);
            
            isPlaying = true;
            
            if (mediaListener != null && currentTrack != null) {
                MediaInfo info = new MediaInfo(currentTrack, currentArtist, currentAlbum, null, totalDuration, null);
                mediaListener.onPlaybackStarted(info);
            }
            
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to send play command: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send pause command
     */
    public boolean pause() {
        if (!isVLCAvailable()) {
            return false;
        }
        
        logger.logInfo("Sending pause command");
        
        try {
            // Try VLC-specific action first
            Intent intent = new Intent(VLC_PAUSE_ACTION);
            intent.setPackage(installedVLCPackage);
            context.sendBroadcast(intent);
            
            // Also try standard media action
            Intent mediaIntent = new Intent(ACTION_PAUSE);
            mediaIntent.setPackage(installedVLCPackage);
            context.sendBroadcast(mediaIntent);
            
            isPlaying = false;
            
            if (mediaListener != null) {
                mediaListener.onPlaybackPaused();
            }
            
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to send pause command: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send stop command
     */
    public boolean stop() {
        if (!isVLCAvailable()) {
            return false;
        }
        
        logger.logInfo("Sending stop command");
        
        try {
            // Try VLC-specific action first
            Intent intent = new Intent(VLC_STOP_ACTION);
            intent.setPackage(installedVLCPackage);
            context.sendBroadcast(intent);
            
            // Also try standard media action
            Intent mediaIntent = new Intent(ACTION_STOP);
            mediaIntent.setPackage(installedVLCPackage);
            context.sendBroadcast(mediaIntent);
            
            // Reset state
            isPlaying = false;
            currentPosition = 0;
            
            if (mediaListener != null) {
                mediaListener.onPlaybackStopped();
            }
            
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to send stop command: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send next track command
     */
    public boolean nextTrack() {
        if (!isVLCAvailable()) {
            return false;
        }
        
        logger.logInfo("Sending next track command");
        
        try {
            // Try VLC-specific action first
            Intent intent = new Intent(VLC_NEXT_ACTION);
            intent.setPackage(installedVLCPackage);
            context.sendBroadcast(intent);
            
            // Also try standard media action
            Intent mediaIntent = new Intent(ACTION_NEXT);
            mediaIntent.setPackage(installedVLCPackage);
            context.sendBroadcast(mediaIntent);
            
            // Simulate track change
            if (mediaListener != null) {
                MediaInfo info = new MediaInfo("Next Track", "Unknown", "Unknown", null, 0, null);
                mediaListener.onTrackChanged(info);
            }
            
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to send next track command: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send previous track command
     */
    public boolean previousTrack() {
        if (!isVLCAvailable()) {
            return false;
        }
        
        logger.logInfo("Sending previous track command");
        
        try {
            // Try VLC-specific action first
            Intent intent = new Intent(VLC_PREVIOUS_ACTION);
            intent.setPackage(installedVLCPackage);
            context.sendBroadcast(intent);
            
            // Also try standard media action
            Intent mediaIntent = new Intent(ACTION_PREVIOUS);
            mediaIntent.setPackage(installedVLCPackage);
            context.sendBroadcast(mediaIntent);
            
            // Simulate track change
            if (mediaListener != null) {
                MediaInfo info = new MediaInfo("Previous Track", "Unknown", "Unknown", null, 0, null);
                mediaListener.onTrackChanged(info);
            }
            
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to send previous track command: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Launch VLC main activity
     */
    public boolean launchVLC() {
        if (!isVLCAvailable()) {
            logger.logError("VLC not available");
            return false;
        }
        
        logger.logInfo("Launching VLC");
        
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(installedVLCPackage);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                
                logger.logInfo("VLC launched successfully");
                return true;
            } else {
                logger.logError("Could not create launch intent for VLC");
                return false;
            }
            
        } catch (Exception e) {
            logger.logError("Failed to launch VLC: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Set audio volume
     */
    public boolean setVolume(int volume) {
        if (audioManager == null) {
            return false;
        }
        
        try {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int targetVolume = (int) ((volume / 100.0f) * maxVolume);
            
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);
            
            logger.logInfo("Volume set to: " + volume + "%");
            return true;
            
        } catch (Exception e) {
            logger.logError("Failed to set volume: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current volume
     */
    public int getVolume() {
        if (audioManager == null) {
            return 0;
        }
        
        try {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            
            return (int) ((currentVolume / (float) maxVolume) * 100);
            
        } catch (Exception e) {
            logger.logError("Failed to get volume: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Extract filename from path or URL
     */
    private String extractFileName(String path) {
        if (path == null) {
            return "Unknown";
        }
        
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        
        return path;
    }
    
    /**
     * Get media status for Android Auto display
     */
    public MediaStatus getMediaStatus() {
        return new MediaStatus(
            isPlaying,
            currentTrack,
            currentArtist,
            currentAlbum,
            currentPosition,
            totalDuration,
            installedVLCPackage
        );
    }
    
    /**
     * Simulate media position updates
     */
    public void simulatePositionUpdate() {
        if (!isPlaying || mediaListener == null) {
            return;
        }
        
        // Simulate position increment
        currentPosition += 1;
        if (currentPosition > totalDuration && totalDuration > 0) {
            currentPosition = totalDuration;
        }
        
        mediaListener.onPositionChanged(currentPosition, totalDuration);
    }
    
    /**
     * Check if VLC supports Android Auto integration
     */
    public boolean supportsAndroidAuto() {
        if (!isVLCAvailable()) {
            return false;
        }
        
        // VLC doesn't have native Android Auto support, but we can control it
        logger.logInfo("VLC Android Auto support: false (using intent-based control)");
        return false; // We use intent-based control instead
    }
    
    /**
     * Media status container
     */
    public static class MediaStatus {
        public final boolean isPlaying;
        public final String currentTrack;
        public final String currentArtist;
        public final String currentAlbum;
        public final int currentPosition;
        public final int totalDuration;
        public final String vlcPackage;
        
        public MediaStatus(boolean isPlaying, String currentTrack, String currentArtist,
                          String currentAlbum, int currentPosition, int totalDuration, String vlcPackage) {
            this.isPlaying = isPlaying;
            this.currentTrack = currentTrack;
            this.currentArtist = currentArtist;
            this.currentAlbum = currentAlbum;
            this.currentPosition = currentPosition;
            this.totalDuration = totalDuration;
            this.vlcPackage = vlcPackage;
        }
    }
    
    // Getters
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public String getCurrentTrack() {
        return currentTrack;
    }
    
    public String getInstalledVLCPackage() {
        return installedVLCPackage;
    }
}