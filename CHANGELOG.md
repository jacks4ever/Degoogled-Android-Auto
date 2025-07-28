# Changelog

## v1.1.0 (2024-07-28)

### Added
- **Full Android Auto Protocol Implementation**
  - Complete protocol implementation with multi-channel communication support
  - Multiple authentication methods (Standard, Nissan-specific, Generic fallback)
  - Comprehensive message parsing and routing system
  - Robust connection state management with automatic retry logic

- **Enhanced USB Data Communication Layer**
  - Proper message framing with CRC32 checksums for data integrity
  - Acknowledgment-based flow control system
  - Real-time USB connection status monitoring
  - Reliable data transmission with error recovery

- **VLC Media Player Integration**
  - Automatic detection of VLC variants (stable, beta, debug)
  - Full playback control with media session integration
  - Rich media notifications with playback controls
  - Automatic playlist loading from device storage

- **Architecture Improvements**
  - Service-based architecture for better reliability
  - Event-driven communication with listener patterns
  - Thread-safe operations with background processing
  - Enhanced logging and diagnostic capabilities

### Enhanced
- Improved USB connection stability and error handling
- Better performance with optimized memory usage
- Enhanced user interface with real-time status updates
- Comprehensive diagnostic information

### Technical
- Updated to target Android API 33
- Enhanced Gradle build configuration
- Added support for Java 17
- Improved dependency management

### Bug Fixes
- Fixed USB communication reliability issues
- Resolved memory leaks in media service
- Fixed manifest merger conflicts
- Resolved Android 12+ exported attribute requirements

## v1.0.1 (2025-07-28)

### Fixed
- Fixed Android SDK environment variable conflict in CI workflow
- Resolved Gradle build issues with proper memory settings
- Enhanced CMake configuration for better error handling
- Added fallback handling for protocol handler source files
- Improved build resilience with proper error handling
- Set C++ standard explicitly in CMake properties

### Security
- Removed unnecessary dependencies that could pose security risks
- Improved error handling to prevent information leakage
- Added proper validation for native code integration

### Added
- Added verbose CMake output for better debugging
- Created local.properties template with correct SDK configuration
- Added release APK files for both debug and release builds

## v1.0.0 (2025-07-25)

### Added
- Initial release of Degoogled Android Auto
- Basic protocol handler implementation
- Support for navigation with OsmAnd
- Support for music with VLC
- Support for phone calls and messaging
- Android Auto interface without Google dependencies