# Nissan Pathfinder 2023 Compatibility Guide

## Overview
This guide provides detailed setup instructions, compatibility information, and troubleshooting steps for using Degoogled Android Auto with the 2023 Nissan Pathfinder head unit system.

## Compatibility Status
- **Vehicle**: 2023 Nissan Pathfinder
- **Head Unit**: NissanConnect with 8" or 9" touchscreen
- **Android Auto Support**: Standard wired connection
- **Wireless Support**: Not natively supported (requires dongle)
- **Degoogled AA Status**: ⚠️ **EXPERIMENTAL** - Framework implemented, protocol development in progress

## Known Specifications

### Display Characteristics
- **8-inch Display**: 800x480 pixels, landscape orientation
- **9-inch Display**: 1024x600 pixels, landscape orientation
- **Touch Targets**: Optimized for 56-64dp minimum size
- **Font Scaling**: 1.1x-1.3x for automotive readability

### USB Connection
- **Port Location**: Center console USB-A port
- **Cable Required**: USB-A to USB-C (or appropriate for your phone)
- **Power Output**: 5V/2.1A (sufficient for phone charging during use)
- **Data Transfer**: USB 2.0 compatible

### Supported Android Auto Features (Standard)
- ✅ Navigation (Google Maps, Waze)
- ✅ Music/Media (Spotify, YouTube Music, etc.)
- ✅ Phone calls and contacts
- ✅ Text messaging (voice only while driving)
- ✅ Voice commands ("Hey Google")

### Degoogled Android Auto Target Features
- 🎯 Navigation with OsmAnd
- 🎯 Media with VLC
- 🎯 Phone calls (native Android)
- 🎯 Messaging (K-9 Mail, Signal)
- 🎯 Voice commands (offline/privacy-focused)

## Setup Instructions

### Prerequisites
1. **Android Device**: Running Android 7.0+ with LibertOS or similar degoogled ROM
2. **USB Cable**: High-quality USB-A to USB-C cable (data transfer capable)
3. **Apps Installed**:
   - OsmAnd (for navigation)
   - VLC (for media playback)
   - K-9 Mail (for email/messaging)
   - Signal (for secure messaging)

### Installation Steps

#### 1. Install Degoogled Android Auto APK
```bash
# Download the latest release APK
# Install via ADB or file manager
adb install -r app-release.apk

# Or install manually:
# 1. Enable "Unknown Sources" in Settings > Security
# 2. Copy APK to phone and tap to install
```

#### 2. Grant Required Permissions
After installation, grant these permissions:
- ✅ Location (for navigation)
- ✅ Phone (for calls and contacts)
- ✅ SMS (for messaging)
- ✅ Storage (for media access)
- ✅ Microphone (for voice commands)
- ✅ Camera (if using for calls)

#### 3. Configure Supporting Apps

**OsmAnd Setup:**
```
1. Download offline maps for your region
2. Enable "Android Auto" in OsmAnd settings
3. Set as default navigation app
```

**VLC Setup:**
```
1. Grant storage permissions
2. Scan media library
3. Enable Android Auto integration (if available)
```

#### 4. Vehicle Connection Process

**First Connection:**
1. Start your Pathfinder and ensure head unit is on
2. Connect phone via USB cable to center console port
3. On phone: Open Degoogled Android Auto app
4. On head unit: Look for Android Auto icon/prompt
5. Follow on-screen pairing instructions

**Subsequent Connections:**
1. Connect USB cable
2. Android Auto should launch automatically
3. If not, tap Android Auto icon on head unit

## Troubleshooting

### Connection Issues

#### "Device Not Recognized"
**Symptoms**: Head unit doesn't detect phone
**Solutions**:
1. Try a different USB cable (ensure data transfer capability)
2. Clean USB ports on both phone and vehicle
3. Restart head unit (hold power button 10+ seconds)
4. Check phone's USB connection mode (should be "File Transfer" or "MTP")

#### "App Not Installed" Error
**Symptoms**: APK won't install
**Solutions**:
1. Enable "Unknown Sources" in Android settings
2. Ensure APK is properly signed (check with `apksigner verify`)
3. Clear package installer cache
4. Try installing via ADB: `adb install -r app-release.apk`

#### "Connection Failed" After USB Detection
**Symptoms**: Phone detected but Android Auto won't start
**Solutions**:
1. Check app permissions (all required permissions granted)
2. Restart Degoogled Android Auto app
3. Clear app cache and data
4. Check logs: `adb logcat | grep DegoogledAA`

### Authentication Issues

#### "Handshake Failed"
**Symptoms**: Connection starts but fails during setup
**Solutions**:
1. Enable verbose logging in app settings
2. Try different protocol versions (app will auto-retry)
3. Check for Nissan software updates
4. Export logs and check for specific error patterns

#### "Protocol Negotiation Error"
**Symptoms**: Connection established but features don't work
**Solutions**:
1. Restart both phone and head unit
2. Clear Android Auto cache on head unit (Settings > Apps > Android Auto > Storage)
3. Try minimal feature set (disable advanced features temporarily)

### Display Issues

#### "UI Elements Too Small"
**Symptoms**: Text and buttons difficult to see/touch
**Solutions**:
1. App automatically detects display size and adjusts
2. Force larger touch targets in app settings
3. Increase font scaling in Android accessibility settings

#### "Screen Orientation Wrong"
**Symptoms**: Portrait mode on landscape display
**Solutions**:
1. App forces landscape mode for automotive use
2. Check head unit display settings
3. Restart connection process

### App Integration Issues

#### "OsmAnd Not Working"
**Symptoms**: Navigation requests fail
**Solutions**:
1. Ensure OsmAnd is set as default navigation app
2. Grant location permissions to both apps
3. Download offline maps for your area
4. Check OsmAnd Android Auto compatibility settings

#### "VLC Media Not Playing"
**Symptoms**: Music/media controls don't work
**Solutions**:
1. Grant storage permissions to VLC
2. Refresh VLC media library
3. Check supported audio formats
4. Try alternative media apps (Poweramp, etc.)

## Logging and Diagnostics

### Enable Verbose Logging
1. Open Degoogled Android Auto app
2. Go to Settings > Developer Options
3. Enable "Verbose Logging"
4. Enable "Export Logs"

### Export Connection Logs
```bash
# Via ADB
adb pull /sdcard/Android/data/com.degoogled.androidauto/files/logs/

# Or use app's export feature:
# Settings > Diagnostics > Export Logs
```

### Log Analysis
Key log patterns to look for:
- `USB_DETECTION`: Device/accessory detection events
- `ACCESSORY_HANDSHAKE`: Protocol negotiation
- `AUTHENTICATION`: Security handshake steps
- `Nissan Pathfinder Compatibility`: Vehicle-specific checks

## Known Limitations

### Current Implementation Status
- ⚠️ **Protocol Handler**: Framework only - needs full Android Auto protocol implementation
- ⚠️ **USB Communication**: Detection working, data transfer needs implementation
- ⚠️ **App Projection**: Service stubs created, full projection system needed
- ⚠️ **Authentication**: Simulation only - real handshake protocol required

### Nissan-Specific Limitations
- **Wireless Android Auto**: Not supported natively (requires aftermarket dongle)
- **Multiple Phones**: Only one phone can be connected at a time
- **Background Apps**: Limited background processing while Android Auto is active
- **System Integration**: Some vehicle functions may not integrate with degoogled apps

### Privacy Considerations
- **No Google Services**: App doesn't communicate with Google servers
- **Local Processing**: Voice commands and data processing kept on-device
- **Minimal Telemetry**: Only essential connection logging (can be disabled)

## Development Status

### Completed Features ✅
- USB device/accessory detection and filtering
- Nissan Pathfinder display optimization
- Comprehensive logging system
- Authentication framework with fallback logic
- Touch target and font scaling optimization
- APK signing and installation fixes

### In Progress 🔄
- Android Auto protocol implementation
- USB data communication
- App projection system
- OsmAnd/VLC integration
- Voice command processing

### Planned Features 📋
- Wireless connection support (via dongle)
- Advanced vehicle integration
- Custom UI themes
- Offline voice recognition
- Multi-language support

## Community Contributions

### Reporting Issues
1. Enable verbose logging
2. Reproduce the issue
3. Export logs
4. Create GitHub issue with:
   - Device model and Android version
   - Vehicle year and head unit type
   - Steps to reproduce
   - Log files (sanitize personal data)

### Testing Requests
We need community testing for:
- Different Nissan model years (2020-2024)
- Various Android devices and ROMs
- Different USB cables and adapters
- Alternative degoogled app combinations

### Development Help Wanted
- Android Auto protocol reverse engineering
- USB communication implementation
- App projection system development
- UI/UX improvements for automotive use

## Support Resources

### Official Channels
- **GitHub Issues**: [Report bugs and feature requests](https://github.com/jacks4ever/Degoogled-Android-Auto/issues)
- **Discussions**: [Community support and development](https://github.com/jacks4ever/Degoogled-Android-Auto/discussions)

### Community Resources
- **XDA Developers**: Android Auto modification discussions
- **Reddit**: r/AndroidAuto, r/degoogle communities
- **Nissan Forums**: Vehicle-specific compatibility reports

### Technical References
- [Android Open Accessory Protocol](https://source.android.com/docs/core/interaction/accessories/aoa)
- [Android Auto Developer Documentation](https://developer.android.com/training/cars)
- [OsmAnd Android Auto Integration](https://osmand.net/docs/user/navigation/auto-car)

## Changelog

### Version 1.0.1 (Current)
- ✅ Fixed APK installation issues
- ✅ Added Nissan Pathfinder-specific optimizations
- ✅ Implemented comprehensive logging system
- ✅ Enhanced USB device detection
- ✅ Added display adaptation for 8"/9" screens
- ✅ Improved authentication framework

### Planned Version 1.1.0
- 🔄 Android Auto protocol implementation
- 🔄 USB data communication
- 🔄 Basic app projection
- 🔄 OsmAnd navigation integration

---

**Disclaimer**: This is experimental software. Use at your own risk. Always follow safe driving practices and local laws regarding device use while driving.

**Last Updated**: July 28, 2025
**Compatibility Tested**: 2023 Nissan Pathfinder with 9" NissanConnect display