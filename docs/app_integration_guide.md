# App Integration Guide

This guide explains how to integrate new apps with the Degoogled Android Auto implementation.

## Overview

The Degoogled Android Auto implementation provides interfaces for integrating different types of apps:

- **Navigation Apps**: For navigation and map display (e.g., OsmAnd)
- **Media Apps**: For music and media playback (e.g., VLC)
- **Phone Apps**: For phone calls and contacts (e.g., Default Phone App)
- **Messaging Apps**: For text messaging (e.g., Default Messaging App)

Each app type has a corresponding interface that must be implemented to integrate the app with the Android Auto implementation.

## General Integration Process

1. **Implement the App Interface**: Create a class that implements the appropriate interface for your app type.
2. **Register the App**: Register your app implementation with the appropriate manager.
3. **Handle App-Specific Messages**: Implement message handlers for app-specific messages.
4. **Create UI Components**: Create UI components for your app.

## Navigation App Integration

### Interface

Navigation apps must implement the `INavigationApp` interface defined in `src/protocol_handler/app/INavigationApp.hpp`:

```cpp
class INavigationApp {
public:
    typedef std::shared_ptr<INavigationApp> Pointer;
    typedef common::Promise<std::vector<SearchResult>> SearchPromise;

    virtual ~INavigationApp() = default;
    virtual void start() = 0;
    virtual void stop() = 0;
    virtual void setNavigationFocus(bool focus) = 0;
    virtual void renderMap(IVideoOutput& videoOutput) = 0;
    virtual void setDestination(const Location& destination) = 0;
    virtual void startNavigation() = 0;
    virtual void stopNavigation() = 0;
    virtual Location getCurrentLocation() = 0;
    virtual void searchLocation(const std::string& query, SearchPromise::Pointer promise) = 0;
};
```

### Example Implementation

See `src/protocol_handler/app/OsmAndNavigationApp.hpp` and `src/protocol_handler/app/OsmAndNavigationApp.cpp` for an example implementation.

### Integration Steps

1. Create a class that implements the `INavigationApp` interface.
2. Implement all required methods.
3. Register your app with the `UIManager` in `src/ui/UIManager.hpp`.
4. Create a UI component for your app in `src/ui/components/`.
5. Implement message handlers for navigation-specific messages in `src/protocol_handler/message/handlers/`.

## Media App Integration

### Interface

Media apps must implement the `IMediaApp` interface defined in `src/protocol_handler/app/IMediaApp.hpp`:

```cpp
class IMediaApp {
public:
    typedef std::shared_ptr<IMediaApp> Pointer;
    typedef common::Promise<std::vector<MediaItem>> BrowsePromise;
    typedef common::Promise<std::vector<MediaItem>> SearchPromise;

    virtual ~IMediaApp() = default;
    virtual void start() = 0;
    virtual void stop() = 0;
    virtual void setMediaFocus(bool focus) = 0;
    virtual void play() = 0;
    virtual void pause() = 0;
    virtual void stop() = 0;
    virtual void next() = 0;
    virtual void previous() = 0;
    virtual void seek(int64_t position) = 0;
    virtual void setVolume(int volume) = 0;
    virtual Track getCurrentTrack() = 0;
    virtual PlaybackState getPlaybackState() = 0;
    virtual void browse(const std::string& path, BrowsePromise::Pointer promise) = 0;
    virtual void search(const std::string& query, SearchPromise::Pointer promise) = 0;
    virtual void playTrack(const std::string& trackId) = 0;
};
```

### Example Implementation

See `src/protocol_handler/app/VLCMediaApp.hpp` and `src/protocol_handler/app/VLCMediaApp.cpp` for an example implementation.

### Integration Steps

1. Create a class that implements the `IMediaApp` interface.
2. Implement all required methods.
3. Register your app with the `UIManager` in `src/ui/UIManager.hpp`.
4. Create a UI component for your app in `src/ui/components/`.
5. Implement message handlers for media-specific messages in `src/protocol_handler/message/handlers/`.

## Phone App Integration

### Interface

Phone apps must implement the `IPhoneApp` interface defined in `src/protocol_handler/app/IPhoneApp.hpp`:

```cpp
class IPhoneApp {
public:
    typedef std::shared_ptr<IPhoneApp> Pointer;
    typedef common::Promise<std::vector<Contact>> ContactsPromise;
    typedef common::Promise<std::vector<Call>> CallHistoryPromise;

    virtual ~IPhoneApp() = default;
    virtual void start() = 0;
    virtual void stop() = 0;
    virtual void setPhoneFocus(bool focus) = 0;
    virtual void dial(const std::string& phoneNumber) = 0;
    virtual void answer() = 0;
    virtual void hangUp() = 0;
    virtual void mute(bool mute) = 0;
    virtual void setSpeaker(bool speaker) = 0;
    virtual Call getCurrentCall() = 0;
    virtual void getContacts(ContactsPromise::Pointer promise) = 0;
    virtual void getCallHistory(CallHistoryPromise::Pointer promise) = 0;
    virtual void searchContacts(const std::string& query, ContactsPromise::Pointer promise) = 0;
};
```

### Example Implementation

See `src/protocol_handler/app/DefaultPhoneApp.hpp` and `src/protocol_handler/app/DefaultPhoneApp.cpp` for an example implementation.

### Integration Steps

1. Create a class that implements the `IPhoneApp` interface.
2. Implement all required methods.
3. Register your app with the `UIManager` in `src/ui/UIManager.hpp`.
4. Create a UI component for your app in `src/ui/components/`.
5. Implement message handlers for phone-specific messages in `src/protocol_handler/message/handlers/`.

## Messaging App Integration

### Interface

Messaging apps must implement the `IMessagingApp` interface defined in `src/protocol_handler/app/IMessagingApp.hpp`:

```cpp
class IMessagingApp {
public:
    typedef std::shared_ptr<IMessagingApp> Pointer;
    typedef common::Promise<std::vector<Conversation>> ConversationsPromise;
    typedef common::Promise<std::vector<Message>> MessagesPromise;

    virtual ~IMessagingApp() = default;
    virtual void start() = 0;
    virtual void stop() = 0;
    virtual void setMessagingFocus(bool focus) = 0;
    virtual void getConversations(ConversationsPromise::Pointer promise) = 0;
    virtual void getMessages(const std::string& conversationId, MessagesPromise::Pointer promise) = 0;
    virtual void sendMessage(const std::string& conversationId, const std::string& message, common::VoidPromise::Pointer promise) = 0;
    virtual void markAsRead(const std::string& conversationId, common::VoidPromise::Pointer promise) = 0;
};
```

### Example Implementation

See `src/protocol_handler/app/DefaultMessagingApp.hpp` and `src/protocol_handler/app/DefaultMessagingApp.cpp` for an example implementation.

### Integration Steps

1. Create a class that implements the `IMessagingApp` interface.
2. Implement all required methods.
3. Register your app with the `UIManager` in `src/ui/UIManager.hpp`.
4. Create a UI component for your app in `src/ui/components/`.
5. Implement message handlers for messaging-specific messages in `src/protocol_handler/message/handlers/`.

## Testing Your Integration

1. Create unit tests for your app implementation in `tests/protocol_handler/app/`.
2. Create integration tests for your app in `tests/integration/`.
3. Run the tests to verify your implementation.

## Best Practices

- **Error Handling**: Always handle errors gracefully and provide meaningful error messages.
- **Asynchronous Operations**: Use promises for asynchronous operations to avoid blocking the main thread.
- **Resource Management**: Properly manage resources, such as file handles and network connections.
- **Thread Safety**: Ensure your implementation is thread-safe, especially when accessing shared resources.
- **Logging**: Use the logging framework to log important events and errors.
- **Documentation**: Document your implementation, especially any non-obvious behavior or requirements.

## Troubleshooting

- **App Not Starting**: Check if the app is properly registered with the appropriate manager.
- **App Not Responding**: Check if the app is properly handling messages and events.
- **App Crashing**: Check for null pointers, out-of-bounds access, and other common programming errors.
- **App Not Rendering**: Check if the app is properly rendering to the video output.
- **App Not Receiving Input**: Check if the app is properly handling input events.

## Conclusion

By following this guide, you should be able to integrate your app with the Degoogled Android Auto implementation. If you encounter any issues or have questions, please refer to the documentation or contact the project maintainers.