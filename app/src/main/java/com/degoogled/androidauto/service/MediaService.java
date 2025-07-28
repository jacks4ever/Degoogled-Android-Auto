package com.degoogled.androidauto.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.core.app.NotificationCompat;

import com.degoogled.androidauto.utils.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simplified VLC media player integration service providing music playback,
 * playlist management, and media control for Android Auto head units.
 */
public class MediaService extends Service {
    private static final String TAG = "MediaService";
    private static final String CHANNEL_ID = "media_channel";
    private static final int NOTIFICATION_ID = 1002;
    
    // VLC package names to try
    private static final String[] VLC_PACKAGES = {
        "org.videolan.vlc",         // VLC for Android
        "org.videolan.vlc.debug",   // VLC debug build
        "org.videolan.vlc.beta"     // VLC beta
    };
    
    // Media actions
    private static final String ACTION_PLAY = "com.degoogled.androidauto.PLAY";
    private static final String ACTION_PAUSE = "com.degoogled.androidauto.PAUSE";
    private static final String ACTION_NEXT = "com.degoogled.androidauto.NEXT";
    private static final String ACTION_PREVIOUS = "com.degoogled.androidauto.PREVIOUS";
    private static final String ACTION_STOP = "com.degoogled.androidauto.STOP";
    
    // Playback states
    private enum PlaybackState {
        STOPPED,
        PLAYING,
        PAUSED,
        BUFFERING,
        ERROR
    }
    
    private final IBinder binder = new MediaBinder();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    private MediaSession mediaSession;
    private AudioManager audioManager;
    private MediaListener mediaListener;
    
    // VLC integration
    private String vlcPackage;
    private boolean vlcAvailable = false;
    
    // Playback state
    private PlaybackState currentState = PlaybackState.STOPPED;
    private MediaTrack currentTrack;
    private List<MediaTrack> playlist;
    private int currentTrackIndex = 0;
    private long currentPosition = 0;
    private long trackDuration = 0;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = false;
    
    public interface MediaListener {
        void onPlaybackStateChanged(PlaybackState state);
        void onTrackChanged(MediaTrack track);
        void onPlaylistChanged(List<MediaTrack> playlist);
        void onPositionChanged(long position, long duration);
        void onMediaError(String error);
    }
    
    public static class MediaTrack {
        public final String id;
        public final String title;
        public final String artist;
        public final String album;
        public final String albumArt;
        public final long duration;
        public final Uri uri;
        
        public MediaTrack(String id, String title, String artist, String album, 
                         String albumArt, long duration, Uri uri) {
            this.id = id;
            this.title = title != null ? title : "Unknown Title";
            this.artist = artist != null ? artist : "Unknown Artist";
            this.album = album != null ? album : "Unknown Album";
            this.albumArt = albumArt;
            this.duration = duration;
            this.uri = uri;
        }
    }
    
    public class MediaBinder extends Binder {
        public MediaService getService() {
            return MediaService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        LogManager.i(TAG, "MediaService created");
        
        createNotificationChannel();
        initializeVLC();
        initializeMediaSession();
        initializeAudioManager();
        loadPlaylist();
        
        startForeground(NOTIFICATION_ID, createNotification());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogManager.i(TAG, "MediaService started");
        
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY:
                        handlePlay();
                        break;
                    case ACTION_PAUSE:
                        handlePause();
                        break;
                    case ACTION_NEXT:
                        handleNext();
                        break;
                    case ACTION_PREVIOUS:
                        handlePrevious();
                        break;
                    case ACTION_STOP:
                        handleStop();
                        break;
                }
            }
        }
        
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        LogManager.i(TAG, "MediaService destroyed");
        
        cleanup();
    }
    
    /**
     * Set media listener
     */
    public void setMediaListener(MediaListener listener) {
        this.mediaListener = listener;
    }
    
    /**
     * Play current track
     */
    public void play() {
        LogManager.i(TAG, "Play requested");
        
        if (currentTrack == null && playlist != null && !playlist.isEmpty()) {
            currentTrack = playlist.get(currentTrackIndex);
        }
        
        if (currentTrack != null) {
            if (vlcAvailable) {
                playWithVLC(currentTrack);
            } else {
                playWithSystemPlayer(currentTrack);
            }
        } else {
            LogManager.w(TAG, "No track to play");
            notifyError("No track available to play");
        }
    }
    
    /**
     * Pause playback
     */
    public void pause() {
        LogManager.i(TAG, "Pause requested");
        setState(PlaybackState.PAUSED);
        updateMediaSession();
    }
    
    /**
     * Stop playback
     */
    public void stop() {
        LogManager.i(TAG, "Stop requested");
        setState(PlaybackState.STOPPED);
        currentPosition = 0;
        updateMediaSession();
    }
    
    /**
     * Skip to next track
     */
    public void next() {
        LogManager.i(TAG, "Next track requested");
        
        if (playlist != null && !playlist.isEmpty()) {
            if (shuffleEnabled) {
                currentTrackIndex = (int) (Math.random() * playlist.size());
            } else {
                currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
            }
            
            currentTrack = playlist.get(currentTrackIndex);
            
            if (currentState == PlaybackState.PLAYING) {
                play();
            } else {
                updateMediaSession();
                sendTrackChanged();
            }
        }
    }
    
    /**
     * Skip to previous track
     */
    public void previous() {
        LogManager.i(TAG, "Previous track requested");
        
        if (playlist != null && !playlist.isEmpty()) {
            if (shuffleEnabled) {
                currentTrackIndex = (int) (Math.random() * playlist.size());
            } else {
                currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
            }
            
            currentTrack = playlist.get(currentTrackIndex);
            
            if (currentState == PlaybackState.PLAYING) {
                play();
            } else {
                updateMediaSession();
                sendTrackChanged();
            }
        }
    }
    
    /**
     * Seek to position
     */
    public void seekTo(long position) {
        LogManager.i(TAG, "Seek to position: " + position);
        currentPosition = position;
        sendPositionUpdate();
    }
    
    /**
     * Toggle shuffle mode
     */
    public void toggleShuffle() {
        shuffleEnabled = !shuffleEnabled;
        LogManager.i(TAG, "Shuffle " + (shuffleEnabled ? "enabled" : "disabled"));
    }
    
    /**
     * Toggle repeat mode
     */
    public void toggleRepeat() {
        repeatEnabled = !repeatEnabled;
        LogManager.i(TAG, "Repeat " + (repeatEnabled ? "enabled" : "disabled"));
    }
    
    /**
     * Get current playback state
     */
    public PlaybackState getCurrentState() {
        return currentState;
    }
    
    /**
     * Get current track
     */
    public MediaTrack getCurrentTrack() {
        return currentTrack;
    }
    
    /**
     * Get playlist
     */
    public List<MediaTrack> getPlaylist() {
        return playlist;
    }
    
    /**
     * Get current position
     */
    public long getCurrentPosition() {
        return currentPosition;
    }
    
    /**
     * Get track duration
     */
    public long getTrackDuration() {
        return trackDuration;
    }
    
    /**
     * Initialize VLC integration
     */
    private void initializeVLC() {
        LogManager.i(TAG, "Initializing VLC integration");
        
        PackageManager pm = getPackageManager();
        
        for (String packageName : VLC_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                vlcPackage = packageName;
                vlcAvailable = true;
                LogManager.i(TAG, "Found VLC package: " + packageName);
                break;
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found, try next
            }
        }
        
        if (!vlcAvailable) {
            LogManager.w(TAG, "VLC not found - using system media player");
        }
    }
    
    /**
     * Initialize media session
     */
    private void initializeMediaSession() {
        LogManager.i(TAG, "Initializing media session");
        
        mediaSession = new MediaSession(this, TAG);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | 
                             MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                handlePlay();
            }
            
            @Override
            public void onPause() {
                handlePause();
            }
            
            @Override
            public void onStop() {
                handleStop();
            }
            
            @Override
            public void onSkipToNext() {
                handleNext();
            }
            
            @Override
            public void onSkipToPrevious() {
                handlePrevious();
            }
            
            @Override
            public void onSeekTo(long pos) {
                seekTo(pos);
            }
        });
        
        mediaSession.setActive(true);
    }
    
    /**
     * Initialize audio manager
     */
    private void initializeAudioManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }
    
    /**
     * Load playlist from device
     */
    private void loadPlaylist() {
        LogManager.i(TAG, "Loading playlist");
        
        executorService.execute(() -> {
            try {
                playlist = new ArrayList<>();
                
                // Query media store for audio files
                String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
                };
                
                android.database.Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Audio.Media.IS_MUSIC + " = 1",
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC"
                );
                
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String id = cursor.getString(0);
                        String title = cursor.getString(1);
                        String artist = cursor.getString(2);
                        String album = cursor.getString(3);
                        long duration = cursor.getLong(4);
                        String data = cursor.getString(5);
                        
                        Uri uri = Uri.parse(data);
                        MediaTrack track = new MediaTrack(id, title, artist, album, null, duration, uri);
                        playlist.add(track);
                    }
                    cursor.close();
                }
                
                LogManager.i(TAG, "Loaded " + playlist.size() + " tracks");
                
                if (mediaListener != null) {
                    mainHandler.post(() -> mediaListener.onPlaylistChanged(playlist));
                }
                
            } catch (Exception e) {
                LogManager.e(TAG, "Error loading playlist: " + e.getMessage());
            }
        });
    }
    
    /**
     * Play track with VLC
     */
    private void playWithVLC(MediaTrack track) {
        try {
            // Launch VLC directly
            Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
            vlcIntent.setDataAndType(track.uri, "audio/*");
            vlcIntent.setPackage(vlcPackage);
            vlcIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(vlcIntent);
            
            setState(PlaybackState.PLAYING);
            
        } catch (Exception e) {
            LogManager.e(TAG, "Error playing with VLC: " + e.getMessage());
            playWithSystemPlayer(track);
        }
    }
    
    /**
     * Play track with system media player
     */
    private void playWithSystemPlayer(MediaTrack track) {
        try {
            Intent playIntent = new Intent(Intent.ACTION_VIEW);
            playIntent.setDataAndType(track.uri, "audio/*");
            playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(playIntent);
            
            setState(PlaybackState.PLAYING);
            
        } catch (Exception e) {
            LogManager.e(TAG, "Error playing with system player: " + e.getMessage());
            notifyError("Failed to play track: " + e.getMessage());
        }
    }
    
    /**
     * Handle play action
     */
    private void handlePlay() {
        play();
    }
    
    /**
     * Handle pause action
     */
    private void handlePause() {
        pause();
    }
    
    /**
     * Handle stop action
     */
    private void handleStop() {
        stop();
    }
    
    /**
     * Handle next action
     */
    private void handleNext() {
        next();
    }
    
    /**
     * Handle previous action
     */
    private void handlePrevious() {
        previous();
    }
    
    /**
     * Set playback state
     */
    private void setState(PlaybackState newState) {
        if (currentState != newState) {
            LogManager.i(TAG, "Playback state changed: " + currentState + " -> " + newState);
            currentState = newState;
            
            if (mediaListener != null) {
                mediaListener.onPlaybackStateChanged(newState);
            }
            
            updateNotification();
        }
    }
    
    /**
     * Update media session
     */
    private void updateMediaSession() {
        if (mediaSession == null) {
            return;
        }
        
        // Update playback state
        android.media.session.PlaybackState.Builder stateBuilder = 
            new android.media.session.PlaybackState.Builder();
        
        int state;
        switch (currentState) {
            case PLAYING:
                state = android.media.session.PlaybackState.STATE_PLAYING;
                break;
            case PAUSED:
                state = android.media.session.PlaybackState.STATE_PAUSED;
                break;
            case BUFFERING:
                state = android.media.session.PlaybackState.STATE_BUFFERING;
                break;
            case ERROR:
                state = android.media.session.PlaybackState.STATE_ERROR;
                break;
            default:
                state = android.media.session.PlaybackState.STATE_STOPPED;
                break;
        }
        
        stateBuilder.setState(state, currentPosition, 1.0f);
        stateBuilder.setActions(
            android.media.session.PlaybackState.ACTION_PLAY |
            android.media.session.PlaybackState.ACTION_PAUSE |
            android.media.session.PlaybackState.ACTION_STOP |
            android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT |
            android.media.session.PlaybackState.ACTION_SKIP_TO_PREVIOUS |
            android.media.session.PlaybackState.ACTION_SEEK_TO
        );
        
        mediaSession.setPlaybackState(stateBuilder.build());
        
        // Update metadata
        if (currentTrack != null) {
            MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, currentTrack.title);
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, currentTrack.artist);
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, currentTrack.album);
            metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, currentTrack.duration);
            
            if (currentTrack.albumArt != null) {
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, currentTrack.albumArt);
            }
            
            mediaSession.setMetadata(metadataBuilder.build());
        }
    }
    
    /**
     * Send track changed notification
     */
    private void sendTrackChanged() {
        if (mediaListener != null && currentTrack != null) {
            mediaListener.onTrackChanged(currentTrack);
        }
    }
    
    /**
     * Send position update
     */
    private void sendPositionUpdate() {
        if (mediaListener != null) {
            mediaListener.onPositionChanged(currentPosition, trackDuration);
        }
    }
    
    /**
     * Notify error
     */
    private void notifyError(String error) {
        LogManager.e(TAG, "Media error: " + error);
        
        if (mediaListener != null) {
            mainHandler.post(() -> mediaListener.onMediaError(error));
        }
        
        setState(PlaybackState.ERROR);
    }
    
    /**
     * Create notification channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Degoogled Android Auto Media");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Create notification
     */
    private Notification createNotification() {
        Intent intent = new Intent(this, MediaService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 
                PendingIntent.FLAG_IMMUTABLE);
        
        String contentText = "Media service running";
        if (currentTrack != null) {
            contentText = currentTrack.title + " - " + currentTrack.artist;
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Degoogled Android Auto")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        
        // Add media controls
        if (currentState == PlaybackState.PLAYING) {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause",
                    createActionIntent(ACTION_PAUSE));
        } else {
            builder.addAction(android.R.drawable.ic_media_play, "Play",
                    createActionIntent(ACTION_PLAY));
        }
        
        builder.addAction(android.R.drawable.ic_media_previous, "Previous",
                createActionIntent(ACTION_PREVIOUS));
        builder.addAction(android.R.drawable.ic_media_next, "Next",
                createActionIntent(ACTION_NEXT));
        
        return builder.build();
    }
    
    /**
     * Create action intent
     */
    private PendingIntent createActionIntent(String action) {
        Intent intent = new Intent(this, MediaService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
    
    /**
     * Update notification
     */
    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, createNotification());
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
        
        executorService.shutdown();
    }
}