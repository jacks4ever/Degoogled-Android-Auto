# Degoogled Android Auto

[![CI](https://github.com/jacks4ever/Degoogled-Android-Auto/actions/workflows/ci.yml/badge.svg)](https://github.com/jacks4ever/Degoogled-Android-Auto/actions/workflows/ci.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A privacy-respecting, Google-free implementation of Android Auto for Unplugged phones running LibertOS, specifically optimized for devices with MediaTek Dimensity 1200 processors.

## Features

- **Navigation**: Integration with OsmAnd for privacy-focused navigation
- **Media**: Integration with VLC for music playback
- **Phone Calls**: Make and receive phone calls
- **Messaging**: Integration with Signal and K-9 Mail for secure messaging
- **Voice Commands**: Offline voice recognition using Vosk
- **Split Screen**: View multiple apps at once
- **Notifications**: Receive notifications from all integrated apps
- **Performance Optimization**: Optimized for MediaTek Dimensity 1200 processors

All without requiring Google services or compromising your privacy.

## Screenshots

[Screenshots will be added here]

## Installation

### Prerequisites

- Unplugged phone running LibertOS
- MediaTek Dimensity 1200 processor
- 8GB RAM
- OsmAnd installed
- VLC installed
- Signal or K-9 Mail installed (optional, for messaging)

### Installation Steps

1. Download the latest release from the [Releases](https://github.com/jacks4ever/Degoogled-Android-Auto/releases) page.
2. Install the APK on your device.
3. Grant the necessary permissions when prompted.
4. Connect your device to your car's head unit using a USB cable.
5. Launch the Degoogled Android Auto app.

For more detailed installation instructions, see the [User Onboarding Guide](docs/user_onboarding.md).

## Documentation

- [User Guide](docs/user_guide.md): Comprehensive guide for users
- [Voice Command Guide](docs/voice_command_guide.md): Guide for using voice commands
- [Performance Optimization Guide](docs/performance_optimization_guide.md): Guide for optimizing performance
- [Integration API](docs/integration_api.md): Documentation for app integrators
- [Security Audit](docs/security_audit.md): Security audit report
- [Release Preparation](docs/release_preparation.md): Guide for preparing releases
- [User Onboarding](docs/user_onboarding.md): Guide for new users
- [Contributor Guide](docs/contributor_guide.md): Guide for contributors

## Project Structure

- `src/`: Source code
  - `protocol_handler/`: Protocol handler code
    - `app/`: App integration code
    - `common/`: Common code
    - `message/`: Message handling code
    - `security/`: Security code
    - `transport/`: Transport code
    - `usb/`: USB communication code
  - `ui/`: UI code
    - `components/`: UI components
  - `voice/`: Voice recognition code
  - `profiler/`: Performance profiling code
- `tests/`: Test code
  - `protocol_handler/`: Protocol handler tests
  - `integration/`: Integration tests
- `docs/`: Documentation
- `build/`: Build directory (created during build)

## Building from Source

### Prerequisites

- C++17 compatible compiler
- CMake 3.10 or higher
- Boost 1.66 or higher
- libusb 1.0
- OpenSSL 1.1 or higher
- Protocol Buffers 3.0 or higher
- ALSA
- nlohmann_json 3.9.0 or higher
- Vosk
- GTest

### Build Instructions

```bash
# Install dependencies (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install -y build-essential cmake libboost-all-dev libssl-dev libusb-1.0-0-dev libprotobuf-dev protobuf-compiler libasound2-dev libgtest-dev nlohmann-json3-dev

# Build and install GTest
cd /usr/src/gtest
sudo cmake .
sudo make
sudo cp lib/*.a /usr/lib

# Install Vosk
sudo pip3 install vosk

# Clone the repository
git clone https://github.com/jacks4ever/Degoogled-Android-Auto.git
cd Degoogled-Android-Auto

# Build the project
mkdir -p build
cd build
cmake ..
make

# Run the tests
ctest
```

## Contributing

We welcome contributions from the community! Please see the [Contributor Guide](docs/contributor_guide.md) for more information on how to contribute to the project.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

This project is based on the following open-source projects:
- [OpenAuto](https://github.com/f1xpl/openauto) - Android Auto headunit emulator
- [aasdk](https://github.com/f1xpl/aasdk) - Android Auto SDK
- [aa4mg](https://github.com/sn-00-x/aa4mg) - Android Auto for MicroG
- [Vosk](https://github.com/alphacep/vosk-api) - Offline speech recognition toolkit

## Contact

- GitHub Issues: [https://github.com/jacks4ever/Degoogled-Android-Auto/issues](https://github.com/jacks4ever/Degoogled-Android-Auto/issues)
- Email: [contact@degoogled-android-auto.org](mailto:contact@degoogled-android-auto.org)

## Disclaimer

This project is not affiliated with Google, Android, or Android Auto. Android Auto is a trademark of Google LLC.