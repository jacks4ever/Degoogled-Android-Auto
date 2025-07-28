# Degoogled Android Auto v1.1.0

## Latest Release

The latest release is **v1.1.0**, which includes major enhancements:
- Full Android Auto protocol implementation
- Enhanced USB data communication layer  
- VLC media player integration
- Improved architecture and reliability

Previous release: [v1.0.1](https://github.com/jacks4ever/Degoogled-Android-Auto/releases/tag/v1.0.1)

## Overview

Degoogled Android Auto is an implementation of Android Auto that works without Google services. It's designed for privacy-focused users who want to use Android Auto features without depending on Google's ecosystem.

## 🚀 New in v1.1.0

### Full Android Auto Protocol Implementation
- Complete protocol implementation with multi-channel communication
- Multiple authentication methods (Standard, Nissan-specific, Generic fallback)
- Robust connection state management with automatic retry logic

### Enhanced USB Data Communication
- Proper message framing with error detection (CRC32 checksums)
- Flow control with acknowledgment system
- Real-time USB connection monitoring

### VLC Media Player Integration
- Automatic VLC detection and integration
- Full playback control (play, pause, stop, next, previous)
- Media session integration with notification controls
- Automatic playlist loading from device storage

### Improved Architecture
- Service-based architecture for better reliability
- Enhanced error handling and recovery mechanisms
- Optimized performance with background processing

For detailed information about all features, see [FEATURES.md](FEATURES.md).

## Features

- Navigation with OsmAnd
- Music playback with VLC
- Phone call support
- Messaging integration
- Works on LibertOS and other degoogled Android ROMs

## Requirements

- Android 7.0 (API level 24) or higher
- MediaTek Dimensity 1200 or equivalent processor
- 8GB RAM recommended
- LibertOS or other AOSP-based ROM without Google services

## Installation

Download the latest release APK from the [Releases](https://github.com/jacks4ever/Degoogled-Android-Auto/releases) page and install it on your device.

## Development

This project is based on the OpenAuto project and aa4mg module, with modifications to work without Google services.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
