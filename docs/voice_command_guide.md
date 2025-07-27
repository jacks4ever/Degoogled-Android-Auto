# Voice Command Guide

This guide explains how to use and extend the voice command functionality in the Degoogled Android Auto implementation.

## Overview

The Degoogled Android Auto implementation includes voice command functionality using the Vosk speech recognition engine. This allows users to control the system using voice commands for navigation, media playback, phone calls, and messaging.

## Voice Command Components

The voice command functionality consists of the following components:

- **IVoiceRecognizer**: Interface for voice recognizers.
- **VoskVoiceRecognizer**: Implementation of the voice recognizer interface using Vosk.
- **VoiceCommandHandler**: Handles voice commands and routes them to the appropriate app.

## Supported Commands

The following voice commands are supported:

### Navigation Commands

- **"Navigate to [destination]"**: Navigates to the specified destination.
- **"Go to [destination]"**: Navigates to the specified destination.

### Media Commands

- **"Play"**: Plays the current track.
- **"Play [track/artist/album]"**: Plays the specified track, artist, or album.
- **"Pause"**: Pauses the current track.
- **"Next"**: Plays the next track.
- **"Previous"**: Plays the previous track.
- **"Skip"**: Plays the next track.
- **"Back"**: Plays the previous track.

### Phone Commands

- **"Call [contact]"**: Calls the specified contact.
- **"Answer"**: Answers an incoming call.
- **"Pick up"**: Answers an incoming call.
- **"Hang up"**: Ends the current call.
- **"End call"**: Ends the current call.

### Messaging Commands

- **"Send message to [contact] say [message]"**: Sends a message to the specified contact.
- **"Read messages"**: Reads unread messages.

### UI Commands

- **"Show [screen]"**: Shows the specified screen (navigation, media, phone, messaging, settings, home).
- **"Go back"**: Goes back to the previous screen.
- **"Return"**: Goes back to the previous screen.
- **"Home"**: Goes to the home screen.

## Using Voice Commands

To use voice commands, follow these steps:

1. Make sure the voice command functionality is enabled in the settings.
2. Press the voice command button or say the wake word (if configured).
3. Speak the command clearly.
4. Wait for the system to process the command.

## Extending Voice Commands

### Adding a New Command

To add a new voice command, follow these steps:

1. Modify the `parseCommand` method in `VoskVoiceRecognizer.cpp` to recognize the new command.
2. Add a handler method for the new command in `VoiceCommandHandler.hpp`.
3. Update the documentation to include the new command.

### Example: Adding a New Command

Let's add a new command to set the volume:

1. Modify the `parseCommand` method in `VoskVoiceRecognizer.cpp`:

```cpp
// Check for volume commands
else if (text.find("set volume to") == 0) {
    command.command = "setvolume";
    size_t pos = text.find("to");
    if (pos != std::string::npos && pos + 3 < text.length()) {
        command.parameters.push_back(text.substr(pos + 3));
    }
}
```

2. Add a handler method for the new command in `VoiceCommandHandler.hpp`:

```cpp
/**
 * @brief Handle a set volume command
 * 
 * @param command Recognized command
 */
void handleSetVolumeCommand(const RecognizedCommand& command) {
    LOG(info) << "Handling set volume command";
    
    // If we have a volume level, set it
    if (!command.parameters.empty()) {
        std::string volumeStr = command.parameters[0];
        LOG(info) << "Setting volume to: " << volumeStr;
        
        // Try to parse the volume level
        int volume = 50; // Default volume
        
        try {
            // Check for percentage
            if (volumeStr.find("%") != std::string::npos) {
                volumeStr = volumeStr.substr(0, volumeStr.find("%"));
            }
            
            // Check for common words
            if (volumeStr.find("max") != std::string::npos || volumeStr.find("full") != std::string::npos) {
                volume = 100;
            } else if (volumeStr.find("min") != std::string::npos || volumeStr.find("mute") != std::string::npos) {
                volume = 0;
            } else if (volumeStr.find("half") != std::string::npos) {
                volume = 50;
            } else {
                // Try to parse as a number
                volume = std::stoi(volumeStr);
            }
            
            // Clamp the volume to the valid range
            volume = std::max(0, std::min(volume, 100));
            
            // Set the volume
            mediaApp_.setVolume(volume);
        } catch (const std::exception& e) {
            LOG(error) << "Failed to parse volume level: " << e.what();
        }
    }
}
```

3. Add the new command to the `handleCommand` method in `VoiceCommandHandler.hpp`:

```cpp
void handleCommand(const RecognizedCommand& command) {
    strand_.dispatch([this, command]() {
        if (!isStarted_) {
            return;
        }
        
        LOG(info) << "Handling voice command: " << command.command << ", text: " << command.text << ", confidence: " << command.confidence;
        
        // Handle the command
        if (command.command == "navigate") {
            handleNavigateCommand(command);
        } else if (command.command == "play") {
            handlePlayCommand(command);
        } else if (command.command == "pause") {
            handlePauseCommand(command);
        } else if (command.command == "next") {
            handleNextCommand(command);
        } else if (command.command == "previous") {
            handlePreviousCommand(command);
        } else if (command.command == "call") {
            handleCallCommand(command);
        } else if (command.command == "answer") {
            handleAnswerCommand(command);
        } else if (command.command == "hangup") {
            handleHangupCommand(command);
        } else if (command.command == "message") {
            handleMessageCommand(command);
        } else if (command.command == "readmessages") {
            handleReadMessagesCommand(command);
        } else if (command.command == "show") {
            handleShowCommand(command);
        } else if (command.command == "back") {
            handleBackCommand(command);
        } else if (command.command == "home") {
            handleHomeCommand(command);
        } else if (command.command == "setvolume") {
            handleSetVolumeCommand(command);
        } else {
            LOG(warning) << "Unknown voice command: " << command.command;
        }
    });
}
```

4. Update the documentation to include the new command:

```markdown
### Media Commands

- **"Set volume to [level]"**: Sets the volume to the specified level (0-100, min, max, half).
```

### Adding a New Voice Recognizer

To add a new voice recognizer, follow these steps:

1. Create a class that implements the `IVoiceRecognizer` interface.
2. Implement all required methods.
3. Update the `VoiceCommandHandler` to use the new voice recognizer.

## Best Practices

When extending the voice command functionality, follow these best practices:

- **Command Recognition**: Make sure the command recognition is robust and can handle variations in how users phrase commands.
- **Error Handling**: Handle errors gracefully and provide meaningful error messages.
- **Feedback**: Provide feedback to the user when a command is recognized and executed.
- **Privacy**: Respect user privacy by only processing voice commands when explicitly requested.
- **Performance**: Optimize the voice recognition for performance, especially on resource-constrained devices.
- **Localization**: Consider supporting multiple languages for voice commands.

## Troubleshooting

- **Command Not Recognized**: Check if the command is supported and if the user is speaking clearly.
- **Command Recognized but Not Executed**: Check if the command handler is properly implemented and if the app is responding to the command.
- **Voice Recognition Not Working**: Check if the voice recognizer is properly initialized and if the microphone is working.
- **High CPU Usage**: Check if the voice recognizer is properly optimized for the device.

## Conclusion

By following this guide, you should be able to use and extend the voice command functionality in the Degoogled Android Auto implementation. If you encounter any issues or have questions, please refer to the documentation or contact the project maintainers.