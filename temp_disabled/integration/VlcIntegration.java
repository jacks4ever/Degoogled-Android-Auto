package com.degoogled.androidauto.integration;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;

import com.degoogled.androidauto.logging.ConnectionLogger;
import com.degoogled.androidauto.protocol.UsbConnection;
import com.degoogled.androidauto.protocol.messages.MessageTypes;
import com.degoogled.androidauto.protocol.messages.media.MediaMetadataResponse;
import com.degoogled.androidauto.protocol.messages.media.MediaPlaybackControl;
import com.degoogled.androidauto.utils.LogManager;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Integration with VLC for media playback.
 * Uses VLC's API to control media playback and get media metadata.
 */
public class VlcIntegration {
    private static final String TAG = "VlcIntegration";
    
    // VLC package and service information
    private static final String VLC_PACKAGE = "org.videolan.vlc";
    private static final String VLC_API_SERVICE = "org.videolan.vlc.api.VlcApiService";
    
    private final Context context;
    private final ConnectionLogger logger;
    private final UsbConnection usbConnection;
    
    private IVlcAidlInterface vlcService;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);
    
    private VlcPlaybackListener playbackListener;
    
    /**
     * Create a new VLC integration
     */
    public VlcIntegration(Context context, ConnectionLogger logger, UsbConnection usbConnection) {
        this.context = context;
        this.logger = logger;
        this.usbConnection = usbConnection;
    }
    
    /**
     * Check if VLC is installed
     */
    public boolean isVlcInstalled() {
        PackageManager pm = context.getPackageManager();
        
        try {
            pm.getPackageInfo(VLC_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Connect to the VLC service
     */
    public boolean connect() {
        if (isConnected.get()) {
            return true;
        }
        
        LogManager.i(TAG, "Connecting to VLC service");
        logger.logConnection("Connecting to VLC service");
        
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(VLC_PACKAGE, VLC_API_SERVICE));
        
        boolean bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        if (!bound) {
            LogManager.e(TAG, "Failed to bind to VLC service");
            logger.logError("Failed to bind to VLC service");
        }
        
        return bound;
    }
    
    /**
     * Disconnect from the VLC service
     */
    public void disconnect() {
        if (!isConnected.get()) {
            return;
        }
        
        LogManager.i(TAG, "Disconnecting from VLC service");
        logger.logConnection("Disconnecting from VLC service");
        
        try {
            if (playbackListener != null) {
                vlcService.removePlaybackListener(playbackListener);
            }
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error removing playback listener", e);
        }
        
        context.unbindService(serviceConnection);
        vlcService = null;
        isConnected.set(false);
    }
    
    /**
     * Play media
     */
    public boolean play() {
        if (!isConnected.get()) {
            LogManager.w(TAG, "Not connected to VLC service");
            return false;
        }
        
        LogManager.i(TAG, "Playing media");
        logger.logConnection("Playing media");
        
        try {
            boolean result = vlcService.play();
            
            if (result) {
                isPlaying.set(true);
            }
            
            return result;
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error playing media", e);
            logger.logError("Error playing media", e);
            return false;
        }
    }
    
    /**
     * Pause media
     */
    public boolean pause() {
        if (!isConnected.get() || !isPlaying.get()) {
            return false;
        }
        
        LogManager.i(TAG, "Pausing media");
        logger.logConnection("Pausing media");
        
        try {
            boolean result = vlcService.pause();
            
            if (result) {
                isPlaying.set(false);
            }
            
            return result;
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error pausing media", e);
            logger.logError("Error pausing media", e);
            return false;
        }
    }
    
    /**
     * Skip to next track
     */
    public boolean next() {
        if (!isConnected.get()) {
            return false;
        }
        
        LogManager.i(TAG, "Skipping to next track");
        logger.logConnection("Skipping to next track");
        
        try {
            return vlcService.next();
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error skipping to next track", e);
            logger.logError("Error skipping to next track", e);
            return false;
        }
    }
    
    /**
     * Skip to previous track
     */
    public boolean previous() {
        if (!isConnected.get()) {
            return false;
        }
        
        LogManager.i(TAG, "Skipping to previous track");
        logger.logConnection("Skipping to previous track");
        
        try {
            return vlcService.previous();
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error skipping to previous track", e);
            logger.logError("Error skipping to previous track", e);
            return false;
        }
    }
    
    /**
     * Seek to a position
     */
    public boolean seekTo(long position) {
        if (!isConnected.get()) {
            return false;
        }
        
        LogManager.i(TAG, "Seeking to position: " + position);
        logger.logConnection("Seeking to position: " + position);
        
        try {
            return vlcService.seekTo(position);
        } catch (RemoteException e) {
            LogManager.e(TAG, "Error seeking to position", e);
            logger.logError("Error seeking to position", e);
            return false;
        }
    }
    
    /**
     * Check if media is playing
     */
    public boolean isPlaying() {
        return isPlaying.get();
    }
    
    /**
     * Handle a media playback control message
     */
    public void handlePlaybackControl(MediaPlaybackControl control) {
        if (!isConnected.get()) {
            return;
        }
        
        int command = control.getCommand();
        
        switch (command) {
            case MediaPlaybackControl.COMMAND_PLAY:
                play();
                break;
                
            case MediaPlaybackControl.COMMAND_PAUSE:
                pause();
                break;
                
            case MediaPlaybackControl.COMMAND_NEXT:
                next();
                break;
                
            case MediaPlaybackControl.COMMAND_PREVIOUS:
                previous();
                break;
                
            case MediaPlaybackControl.COMMAND_SEEK:
                seekTo(control.getSeekPosition());
                break;
                
            case MediaPlaybackControl.COMMAND_STOP:
                pause();
                break;
        }
    }
    
    /**
     * Service connection for VLC
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogManager.i(TAG, "Connected to VLC service");
            logger.logConnection("Connected to VLC service");
            
            vlcService = IVlcAidlInterface.Stub.asInterface(service);
            isConnected.set(true);
            
            try {
                // Register a playback listener
                playbackListener = new VlcPlaybackListener();
                vlcService.addPlaybackListener(playbackListener);
                
                // Get the current playback state
                isPlaying.set(vlcService.isPlaying());
            } catch (RemoteException e) {
                LogManager.e(TAG, "Error initializing VLC service", e);
                logger.logError("Error initializing VLC service", e);
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogManager.i(TAG, "Disconnected from VLC service");
            logger.logConnection("Disconnected from VLC service");
            
            vlcService = null;
            isConnected.set(false);
            isPlaying.set(false);
        }
    };
    
    /**
     * Playback listener for VLC
     */
    private class VlcPlaybackListener extends IVlcPlaybackListener.Stub {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) throws RemoteException {
            LogManager.d(TAG, "Playback state changed: " + state.isPlaying());
            
            isPlaying.set(state.isPlaying());
        }
        
        @Override
        public void onMetadataChanged(MediaMetadata metadata) throws RemoteException {
            LogManager.d(TAG, "Metadata changed: " + metadata.getTitle());
            
            // Get the album art
            byte[] albumArt = getAlbumArt(metadata);
            
            // Create and send a metadata response message
            MediaMetadataResponse metadataResponse = new MediaMetadataResponse(
                    MessageTypes.STATUS_OK,
                    metadata.getTitle(),
                    metadata.getArtist(),
                    metadata.getAlbum(),
                    metadata.getDuration(),
                    albumArt
            );
            
            usbConnection.sendMessage(metadataResponse);
        }
    }
    
    /**
     * Get album art for the given metadata
     */
    private byte[] getAlbumArt(MediaMetadata metadata) {
        // In a real implementation, this would get the album art from VLC
        // For now, we'll return a placeholder
        
        // Create a simple bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_gallery);
        
        if (bitmap == null) {
            return new byte[0];
        }
        
        // Convert the bitmap to a byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        return stream.toByteArray();
    }
    
    /**
     * VLC AIDL interface
     */
    public interface IVlcAidlInterface {
        boolean play() throws RemoteException;
        boolean pause() throws RemoteException;
        boolean next() throws RemoteException;
        boolean previous() throws RemoteException;
        boolean seekTo(long position) throws RemoteException;
        boolean isPlaying() throws RemoteException;
        void addPlaybackListener(VlcPlaybackListener listener) throws RemoteException;
        void removePlaybackListener(VlcPlaybackListener listener) throws RemoteException;
        
        abstract class Stub implements IVlcAidlInterface {
            public static IVlcAidlInterface asInterface(IBinder binder) {
                // In a real implementation, this would use the actual AIDL-generated code
                return null;
            }
        }
    }
    
    /**
     * VLC playback listener interface
     */
    public interface IVlcPlaybackListener {
        void onPlaybackStateChanged(PlaybackState state) throws RemoteException;
        void onMetadataChanged(MediaMetadata metadata) throws RemoteException;
        
        abstract class Stub implements IVlcPlaybackListener {
            // In a real implementation, this would use the actual AIDL-generated code
        }
    }
    
    /**
     * Playback state from VLC
     */
    public static class PlaybackState {
        private boolean isPlaying;
        private long position;
        private long duration;
        
        public boolean isPlaying() {
            return isPlaying;
        }
        
        public void setPlaying(boolean playing) {
            isPlaying = playing;
        }
        
        public long getPosition() {
            return position;
        }
        
        public void setPosition(long position) {
            this.position = position;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
    
    /**
     * Media metadata from VLC
     */
    public static class MediaMetadata {
        private String title;
        private String artist;
        private String album;
        private long duration;
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getArtist() {
            return artist;
        }
        
        public void setArtist(String artist) {
            this.artist = artist;
        }
        
        public String getAlbum() {
            return album;
        }
        
        public void setAlbum(String album) {
            this.album = album;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
}