# Vehicle Compatibility Guide

## Nissan Pathfinder (2023) Compatibility

The Degoogled Android Auto implementation should work with the 2023 Nissan Pathfinder's head unit display, but there are some important considerations:

### Current Status

- **Basic USB Connection**: The app includes USB accessory filters that should allow it to be recognized when connected to the vehicle's USB port.
- **Protocol Support**: The implementation includes a basic protocol handler service that mimics the Android Auto protocol without Google services.
- **Head Unit Integration**: The app should be detected by the Nissan Pathfinder's infotainment system when connected via USB.

### Known Limitations

1. **Authentication**: Some vehicle head units may expect Google's authentication protocol, which this implementation bypasses.
2. **Manufacturer-Specific Features**: Nissan-specific features may not be fully supported.
3. **Wireless Connection**: Currently only USB connections are supported; wireless Android Auto is not implemented.
4. **Voice Commands**: Voice command integration with the vehicle's system may be limited.

### Setup Instructions for Nissan Pathfinder

1. Install the Degoogled Android Auto APK on your device.
2. Enable Developer Options on your Android device.
3. Enable USB Debugging in Developer Options.
4. Connect your device to the Nissan Pathfinder's USB port using a high-quality data cable.
5. When prompted on your phone, allow the USB connection for Android Auto.
6. On the vehicle's display, select the Android Auto option if it doesn't launch automatically.

### Troubleshooting

If the connection is not established:

1. Try using different USB cables - some cables are charge-only and don't support data transfer.
2. Restart both your phone and the vehicle's infotainment system.
3. Check if your phone is recognized as a USB device by the vehicle.
4. Ensure the app has all required permissions granted.

## General Vehicle Compatibility

This implementation has been designed to work with most vehicles that support Android Auto, but compatibility cannot be guaranteed for all models and manufacturers. The implementation focuses on the core protocol without relying on Google services.

### Supported Features

- Navigation using OsmAnd
- Music playback with VLC
- Phone calls
- Messaging

### Testing Status

This implementation is in active development and has been tested with limited vehicle models. User feedback on compatibility with specific vehicles is welcome.