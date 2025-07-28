package com.degoogled.androidauto.protocol.messages;

import com.degoogled.androidauto.protocol.messages.auth.AuthChallengeRequest;
import com.degoogled.androidauto.protocol.messages.auth.AuthChallengeResponse;
import com.degoogled.androidauto.protocol.messages.auth.AuthCompleteRequest;
import com.degoogled.androidauto.protocol.messages.auth.AuthCompleteResponse;
import com.degoogled.androidauto.protocol.messages.auth.AuthStartRequest;
import com.degoogled.androidauto.protocol.messages.auth.AuthStartResponse;
import com.degoogled.androidauto.protocol.messages.control.ControlVersionRequest;
import com.degoogled.androidauto.protocol.messages.control.ControlVersionResponse;
import com.degoogled.androidauto.protocol.messages.control.ServiceDiscoveryRequest;
import com.degoogled.androidauto.protocol.messages.control.ServiceDiscoveryResponse;
import com.degoogled.androidauto.protocol.messages.media.MediaMetadataRequest;
import com.degoogled.androidauto.protocol.messages.media.MediaMetadataResponse;
import com.degoogled.androidauto.protocol.messages.media.MediaPlaybackControl;
import com.degoogled.androidauto.protocol.messages.media.MediaSetupRequest;
import com.degoogled.androidauto.protocol.messages.media.MediaSetupResponse;
import com.degoogled.androidauto.protocol.messages.nav.NavSetupRequest;
import com.degoogled.androidauto.protocol.messages.nav.NavSetupResponse;
import com.degoogled.androidauto.protocol.messages.nav.NavStartRequest;
import com.degoogled.androidauto.protocol.messages.nav.NavStartResponse;
import com.degoogled.androidauto.protocol.messages.nav.NavTurnEvent;
import com.degoogled.androidauto.utils.LogManager;

/**
 * Factory for creating message objects from raw data.
 */
public class MessageFactory {
    private static final String TAG = "MessageFactory";
    
    /**
     * Create a message object from the channel ID, message type, and payload
     */
    public static Message createMessage(int channelId, int messageType, byte[] payload) {
        switch (channelId) {
            case MessageTypes.CHANNEL_CONTROL:
                return createControlMessage(messageType, payload);
                
            case MessageTypes.CHANNEL_AUTH:
                return createAuthMessage(messageType, payload);
                
            case MessageTypes.CHANNEL_MEDIA:
                return createMediaMessage(messageType, payload);
                
            case MessageTypes.CHANNEL_NAVIGATION:
                return createNavMessage(messageType, payload);
                
            default:
                LogManager.w(TAG, "Unknown channel ID: " + channelId);
                return new UnknownMessage(channelId, messageType, payload);
        }
    }
    
    /**
     * Create a control channel message
     */
    private static Message createControlMessage(int messageType, byte[] payload) {
        switch (messageType) {
            case MessageTypes.CONTROL_MSG_VERSION_REQUEST:
                return new ControlVersionRequest(payload);
                
            case MessageTypes.CONTROL_MSG_VERSION_RESPONSE:
                return new ControlVersionResponse(payload);
                
            case MessageTypes.CONTROL_MSG_SERVICE_DISCOVERY_REQUEST:
                return new ServiceDiscoveryRequest(payload);
                
            case MessageTypes.CONTROL_MSG_SERVICE_DISCOVERY_RESPONSE:
                return new ServiceDiscoveryResponse(payload);
                
            default:
                LogManager.w(TAG, "Unknown control message type: " + messageType);
                return new UnknownMessage(MessageTypes.CHANNEL_CONTROL, messageType, payload);
        }
    }
    
    /**
     * Create an authentication channel message
     */
    private static Message createAuthMessage(int messageType, byte[] payload) {
        switch (messageType) {
            case MessageTypes.AUTH_MSG_START_REQUEST:
                return new AuthStartRequest(payload);
                
            case MessageTypes.AUTH_MSG_START_RESPONSE:
                return new AuthStartResponse(payload);
                
            case MessageTypes.AUTH_MSG_CHALLENGE_REQUEST:
                return new AuthChallengeRequest(payload);
                
            case MessageTypes.AUTH_MSG_CHALLENGE_RESPONSE:
                return new AuthChallengeResponse(payload);
                
            case MessageTypes.AUTH_MSG_COMPLETE_REQUEST:
                return new AuthCompleteRequest(payload);
                
            case MessageTypes.AUTH_MSG_COMPLETE_RESPONSE:
                return new AuthCompleteResponse(payload);
                
            default:
                LogManager.w(TAG, "Unknown auth message type: " + messageType);
                return new UnknownMessage(MessageTypes.CHANNEL_AUTH, messageType, payload);
        }
    }
    
    /**
     * Create a media channel message
     */
    private static Message createMediaMessage(int messageType, byte[] payload) {
        switch (messageType) {
            case MessageTypes.MEDIA_MSG_SETUP_REQUEST:
                return new MediaSetupRequest(payload);
                
            case MessageTypes.MEDIA_MSG_SETUP_RESPONSE:
                return new MediaSetupResponse(payload);
                
            case MessageTypes.MEDIA_MSG_METADATA_REQUEST:
                return new MediaMetadataRequest(payload);
                
            case MessageTypes.MEDIA_MSG_METADATA_RESPONSE:
                return new MediaMetadataResponse(payload);
                
            case MessageTypes.MEDIA_MSG_PLAYBACK_CONTROL:
                return new MediaPlaybackControl(payload);
                
            default:
                LogManager.w(TAG, "Unknown media message type: " + messageType);
                return new UnknownMessage(MessageTypes.CHANNEL_MEDIA, messageType, payload);
        }
    }
    
    /**
     * Create a navigation channel message
     */
    private static Message createNavMessage(int messageType, byte[] payload) {
        switch (messageType) {
            case MessageTypes.NAV_MSG_SETUP_REQUEST:
                return new NavSetupRequest(payload);
                
            case MessageTypes.NAV_MSG_SETUP_RESPONSE:
                return new NavSetupResponse(payload);
                
            case MessageTypes.NAV_MSG_START_REQUEST:
                return new NavStartRequest(payload);
                
            case MessageTypes.NAV_MSG_START_RESPONSE:
                return new NavStartResponse(payload);
                
            case MessageTypes.NAV_MSG_TURN_EVENT:
                return new NavTurnEvent(payload);
                
            default:
                LogManager.w(TAG, "Unknown nav message type: " + messageType);
                return new UnknownMessage(MessageTypes.CHANNEL_NAVIGATION, messageType, payload);
        }
    }
}