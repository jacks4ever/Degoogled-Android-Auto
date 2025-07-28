package com.degoogled.androidauto.protocol.messages;

/**
 * Defines the message types used in the Android Auto protocol.
 * Based on reverse engineering of the protocol and documentation.
 */
public class MessageTypes {
    // Control channel messages
    public static final int CONTROL_MSG_VERSION_REQUEST = 0x01;
    public static final int CONTROL_MSG_VERSION_RESPONSE = 0x02;
    public static final int CONTROL_MSG_SERVICE_DISCOVERY_REQUEST = 0x03;
    public static final int CONTROL_MSG_SERVICE_DISCOVERY_RESPONSE = 0x04;
    public static final int CONTROL_MSG_PING_REQUEST = 0x0D;
    public static final int CONTROL_MSG_PING_RESPONSE = 0x0E;
    public static final int CONTROL_MSG_NAV_FOCUS_REQUEST = 0x0F;
    public static final int CONTROL_MSG_NAV_FOCUS_RESPONSE = 0x10;
    public static final int CONTROL_MSG_BYEBYE_REQUEST = 0x11;
    public static final int CONTROL_MSG_BYEBYE_RESPONSE = 0x12;
    public static final int CONTROL_MSG_VOICE_SESSION_REQUEST = 0x13;
    public static final int CONTROL_MSG_VOICE_SESSION_RESPONSE = 0x14;
    public static final int CONTROL_MSG_AUDIO_FOCUS_REQUEST = 0x15;
    public static final int CONTROL_MSG_AUDIO_FOCUS_RESPONSE = 0x16;
    public static final int CONTROL_MSG_STATUS_REQUEST = 0x17;
    public static final int CONTROL_MSG_STATUS_RESPONSE = 0x18;
    public static final int CONTROL_MSG_NAVIGATION_STATUS_REQUEST = 0x19;
    public static final int CONTROL_MSG_NAVIGATION_STATUS_RESPONSE = 0x1A;
    public static final int CONTROL_MSG_MEDIA_STATUS_REQUEST = 0x1B;
    public static final int CONTROL_MSG_MEDIA_STATUS_RESPONSE = 0x1C;
    
    // Authentication channel messages
    public static final int AUTH_MSG_START_REQUEST = 0x01;
    public static final int AUTH_MSG_START_RESPONSE = 0x02;
    public static final int AUTH_MSG_CHALLENGE_REQUEST = 0x03;
    public static final int AUTH_MSG_CHALLENGE_RESPONSE = 0x04;
    public static final int AUTH_MSG_COMPLETE_REQUEST = 0x05;
    public static final int AUTH_MSG_COMPLETE_RESPONSE = 0x06;
    public static final int AUTH_MSG_ERROR = 0xFF;
    
    // Input channel messages
    public static final int INPUT_MSG_TOUCH_EVENT = 0x01;
    public static final int INPUT_MSG_KEY_EVENT = 0x02;
    public static final int INPUT_MSG_RELATIVE_INPUT_EVENT = 0x03;
    public static final int INPUT_MSG_SCROLL_EVENT = 0x04;
    public static final int INPUT_MSG_BUTTON_EVENT = 0x05;
    
    // Video channel messages
    public static final int VIDEO_MSG_SETUP_REQUEST = 0x01;
    public static final int VIDEO_MSG_SETUP_RESPONSE = 0x02;
    public static final int VIDEO_MSG_START_REQUEST = 0x03;
    public static final int VIDEO_MSG_START_RESPONSE = 0x04;
    public static final int VIDEO_MSG_STOP_REQUEST = 0x05;
    public static final int VIDEO_MSG_STOP_RESPONSE = 0x06;
    public static final int VIDEO_MSG_FRAME_DATA = 0x07;
    public static final int VIDEO_MSG_HEARTBEAT = 0x08;
    
    // Media channel messages
    public static final int MEDIA_MSG_SETUP_REQUEST = 0x01;
    public static final int MEDIA_MSG_SETUP_RESPONSE = 0x02;
    public static final int MEDIA_MSG_START_REQUEST = 0x03;
    public static final int MEDIA_MSG_START_RESPONSE = 0x04;
    public static final int MEDIA_MSG_STOP_REQUEST = 0x05;
    public static final int MEDIA_MSG_STOP_RESPONSE = 0x06;
    public static final int MEDIA_MSG_METADATA_REQUEST = 0x07;
    public static final int MEDIA_MSG_METADATA_RESPONSE = 0x08;
    public static final int MEDIA_MSG_PLAYBACK_CONTROL = 0x09;
    public static final int MEDIA_MSG_PLAYBACK_STATUS = 0x0A;
    
    // Navigation channel messages
    public static final int NAV_MSG_SETUP_REQUEST = 0x01;
    public static final int NAV_MSG_SETUP_RESPONSE = 0x02;
    public static final int NAV_MSG_START_REQUEST = 0x03;
    public static final int NAV_MSG_START_RESPONSE = 0x04;
    public static final int NAV_MSG_STOP_REQUEST = 0x05;
    public static final int NAV_MSG_STOP_RESPONSE = 0x06;
    public static final int NAV_MSG_NAVIGATION_STATUS = 0x07;
    public static final int NAV_MSG_NAVIGATION_COMMAND = 0x08;
    public static final int NAV_MSG_TURN_EVENT = 0x09;
    
    // Phone channel messages
    public static final int PHONE_MSG_SETUP_REQUEST = 0x01;
    public static final int PHONE_MSG_SETUP_RESPONSE = 0x02;
    public static final int PHONE_MSG_INCOMING_CALL = 0x03;
    public static final int PHONE_MSG_CALL_ACTION = 0x04;
    public static final int PHONE_MSG_CALL_STATUS = 0x05;
    
    // Messaging channel messages
    public static final int MSG_SETUP_REQUEST = 0x01;
    public static final int MSG_SETUP_RESPONSE = 0x02;
    public static final int MSG_NOTIFICATION = 0x03;
    public static final int MSG_ACTION = 0x04;
    
    // Channel IDs
    public static final int CHANNEL_CONTROL = 0x01;
    public static final int CHANNEL_AUTH = 0x02;
    public static final int CHANNEL_INPUT = 0x03;
    public static final int CHANNEL_VIDEO = 0x04;
    public static final int CHANNEL_MEDIA = 0x05;
    public static final int CHANNEL_NAVIGATION = 0x06;
    public static final int CHANNEL_PHONE = 0x07;
    public static final int CHANNEL_MESSAGING = 0x08;
    
    // Service IDs
    public static final int SERVICE_CONTROL = 0x0001;
    public static final int SERVICE_AUTH = 0x0002;
    public static final int SERVICE_INPUT = 0x0003;
    public static final int SERVICE_VIDEO = 0x0004;
    public static final int SERVICE_MEDIA = 0x0005;
    public static final int SERVICE_NAVIGATION = 0x0006;
    public static final int SERVICE_PHONE = 0x0007;
    public static final int SERVICE_MESSAGING = 0x0008;
    
    // Status codes
    public static final int STATUS_OK = 0x00;
    public static final int STATUS_FAIL = 0x01;
    public static final int STATUS_UNSUPPORTED = 0x02;
    public static final int STATUS_INVALID_PARAMETER = 0x03;
    public static final int STATUS_NOT_AUTHORIZED = 0x04;
    public static final int STATUS_TIMEOUT = 0x05;
}