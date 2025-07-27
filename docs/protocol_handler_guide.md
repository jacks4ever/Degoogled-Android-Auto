# Protocol Handler Guide

This guide explains the protocol handler component of the Degoogled Android Auto implementation.

## Overview

The protocol handler is responsible for handling the Android Auto protocol, including message serialization/deserialization, transport, and app integration. It is the core component of the Degoogled Android Auto implementation.

## Architecture

The protocol handler is organized into the following components:

- **Common**: Common utilities and data structures.
- **Message**: Message serialization/deserialization and handling.
- **Transport**: Transport layer implementation.
- **USB**: USB-specific functionality.
- **Security**: Encryption, authentication, and certificate management.
- **App**: App integration interfaces and implementations.

### Common

The common component provides utilities and data structures used throughout the protocol handler. It includes:

- **DataBuffer**: A buffer for storing data.
- **Promise**: A promise implementation for asynchronous operations.
- **Error**: Error handling utilities.
- **Log**: Logging utilities.

### Message

The message component handles message serialization/deserialization and routing. It includes:

- **Message**: Represents a message in the Android Auto protocol.
- **MessageHeader**: Represents the header of a message.
- **MessageType**: Enum representing the message types.
- **MessageSerializer**: Serializes and deserializes messages.
- **IMessageHandler**: Interface for message handlers.
- **MessageDispatcher**: Dispatches messages to the appropriate handlers.
- **Handlers**: Implementations of message handlers for specific message types.

### Transport

The transport component handles the transport layer. It includes:

- **ITransport**: Interface for transport implementations.
- **USBTransport**: USB transport implementation.
- **TCPTransport**: TCP transport implementation.

### USB

The USB component provides USB-specific functionality. It includes:

- **USBWrapper**: Wrapper for libusb.
- **USBAccessory**: Represents a USB accessory.
- **USBAccessoryModeQuery**: Queries for USB accessory mode.

### Security

The security component handles encryption, authentication, and certificate management. It includes:

- **ICryptoProvider**: Interface for crypto providers.
- **OpenSSLCryptoProvider**: OpenSSL implementation of the crypto provider interface.
- **AuthenticationManager**: Manages authentication with the head unit.
- **CertificateManager**: Manages certificates for authentication.

### App

The app component provides interfaces and implementations for app integration. It includes:

- **INavigationApp**: Interface for navigation apps.
- **IMediaApp**: Interface for media apps.
- **IPhoneApp**: Interface for phone apps.
- **IMessagingApp**: Interface for messaging apps.
- **OsmAndNavigationApp**: Implementation of the navigation app interface for OsmAnd.
- **VLCMediaApp**: Implementation of the media app interface for VLC.
- **DefaultPhoneApp**: Implementation of the phone app interface for the default phone app.
- **DefaultMessagingApp**: Implementation of the messaging app interface for the default messaging app.

## Message Flow

The message flow in the protocol handler is as follows:

1. A message is received from the transport layer.
2. The message is deserialized by the message serializer.
3. The message is dispatched to the appropriate handler by the message dispatcher.
4. The handler processes the message and generates a response.
5. The response is serialized by the message serializer.
6. The response is sent through the transport layer.

## Authentication Flow

The authentication flow in the protocol handler is as follows:

1. The head unit sends an authentication start message.
2. The authentication manager receives the message and extracts the certificate.
3. The certificate manager verifies the certificate.
4. The authentication manager sends an authentication status message.
5. The head unit sends an authentication complete message.
6. The authentication manager updates the authentication status.

## App Integration

The protocol handler provides interfaces for integrating different types of apps:

- **Navigation Apps**: For navigation and map display.
- **Media Apps**: For music and media playback.
- **Phone Apps**: For phone calls and contacts.
- **Messaging Apps**: For text messaging.

Each app type has a corresponding interface that must be implemented to integrate the app with the Android Auto implementation. See the [App Integration Guide](app_integration_guide.md) for more information.

## Extending the Protocol Handler

### Adding a New Message Type

To add a new message type, follow these steps:

1. Add the message type to the `MessageType` enum in `src/protocol_handler/message/MessageType.hpp`.
2. Add a string representation of the message type to the `messageTypeToString` function in `src/protocol_handler/message/MessageType.hpp`.
3. Create a message handler for the new message type in `src/protocol_handler/message/handlers/`.
4. Register the message handler with the message dispatcher.

### Adding a New Transport

To add a new transport, follow these steps:

1. Create a class that implements the `ITransport` interface in `src/protocol_handler/transport/`.
2. Implement all required methods.
3. Register the transport with the appropriate manager.

### Adding a New Crypto Provider

To add a new crypto provider, follow these steps:

1. Create a class that implements the `ICryptoProvider` interface in `src/protocol_handler/security/`.
2. Implement all required methods.
3. Register the crypto provider with the appropriate manager.

## Best Practices

- **Error Handling**: Always handle errors gracefully and provide meaningful error messages.
- **Asynchronous Operations**: Use promises for asynchronous operations to avoid blocking the main thread.
- **Resource Management**: Properly manage resources, such as file handles and network connections.
- **Thread Safety**: Ensure your implementation is thread-safe, especially when accessing shared resources.
- **Logging**: Use the logging framework to log important events and errors.
- **Documentation**: Document your implementation, especially any non-obvious behavior or requirements.

## Troubleshooting

- **Message Not Being Handled**: Check if the message handler is registered with the message dispatcher.
- **Transport Not Working**: Check if the transport is properly initialized and connected.
- **Authentication Failing**: Check if the certificates are valid and properly configured.
- **App Not Responding**: Check if the app is properly handling messages and events.

## Conclusion

By understanding the protocol handler component, you should be able to extend and customize the Degoogled Android Auto implementation to suit your needs. If you encounter any issues or have questions, please refer to the documentation or contact the project maintainers.