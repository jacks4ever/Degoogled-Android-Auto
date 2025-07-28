# Changelog

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