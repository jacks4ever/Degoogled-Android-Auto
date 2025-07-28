# Release Notes for v1.0.1

## Overview
This release focuses on fixing the Android build issues in the CI workflow and improving the overall build stability. The application now successfully passes all CI checks including android-build, cpp-analysis, and security-scan.

## What's New
- Fixed Android SDK environment variable conflicts
- Enhanced build configuration for better reliability
- Added proper error handling for missing components
- Improved security with better validation and error handling

## Installation
Download the appropriate APK file for your device:
- [Debug Build](releases/degoogled-android-auto-v1.0.1-debug.apk)
- [Release Build](releases/degoogled-android-auto-v1.0.1-release.apk)

## Requirements
- Android 7.0 (API level 24) or higher
- MediaTek Dimensity 1200 or equivalent processor
- 8GB RAM recommended
- LibertOS or other AOSP-based ROM without Google services

## Compatibility
- Tested with OsmAnd for navigation
- Tested with VLC for music playback
- Supports basic phone call functionality
- Supports messaging integration

## Known Issues
- May require manual permission granting on first run
- Some head units may require additional configuration

## Security Notes
- This release has passed all security scans in the CI workflow
- No sensitive data is collected or transmitted
- All communication is handled locally without Google services

## Feedback
Please report any issues or suggestions through the GitHub issue tracker.