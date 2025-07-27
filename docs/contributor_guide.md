# Contributor Guide

## Welcome to the Degoogled Android Auto Project!

Thank you for your interest in contributing to the Degoogled Android Auto project! This guide will help you get started as a contributor and provide information on how to make meaningful contributions to the project.

## Project Overview

Degoogled Android Auto is an open-source implementation of Android Auto that does not rely on Google services. It provides a similar experience to Android Auto, but with a focus on privacy and freedom.

The project consists of several components:

- **Protocol Handler**: Handles communication with the car's head unit.
- **App Integrations**: Integrates with apps like OsmAnd, VLC, Signal, and K-9 Mail.
- **UI Components**: Provides a user interface for navigation, media, phone, and messaging.
- **Voice Recognition**: Provides voice command functionality using Vosk.
- **Performance Optimization**: Optimizes performance for MediaTek Dimensity 1200 processors.

## Getting Started

### Prerequisites

Before you start contributing, make sure you have:

- A basic understanding of C++
- Familiarity with CMake
- Familiarity with Git and GitHub
- A development environment set up for C++ development

### Setting Up the Development Environment

1. **Clone the Repository**:

```bash
git clone https://github.com/jacks4ever/Degoogled-Android-Auto.git
cd Degoogled-Android-Auto
```

2. **Install Dependencies**:

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y build-essential cmake libboost-all-dev libssl-dev libusb-1.0-0-dev libprotobuf-dev protobuf-compiler libasound2-dev libgtest-dev nlohmann-json3-dev

# Build and install GTest
cd /usr/src/gtest
sudo cmake .
sudo make
sudo cp lib/*.a /usr/lib

# Install Vosk
sudo pip3 install vosk
```

3. **Build the Project**:

```bash
mkdir -p build
cd build
cmake ..
make
```

4. **Run the Tests**:

```bash
ctest
```

## Contributing

### Finding Issues to Work On

You can find issues to work on in the GitHub issue tracker. Issues labeled "good first issue" are a good place to start for new contributors.

### Making Changes

1. **Create a Branch**:

```bash
git checkout -b feature/your-feature-name
```

2. **Make Your Changes**:

Make your changes to the codebase. Make sure to follow the coding style and best practices.

3. **Write Tests**:

Write tests for your changes to ensure they work as expected and don't break existing functionality.

4. **Run the Tests**:

```bash
cd build
make
ctest
```

5. **Commit Your Changes**:

```bash
git add .
git commit -m "Add your feature or fix"
```

6. **Push Your Changes**:

```bash
git push origin feature/your-feature-name
```

7. **Create a Pull Request**:

Go to the GitHub repository and create a pull request from your branch to the main branch.

### Code Style

We follow the Google C++ Style Guide with some modifications. Please make sure your code adheres to this style guide.

Some key points:

- Use 4 spaces for indentation, not tabs.
- Use camelCase for variable and function names.
- Use PascalCase for class names.
- Use snake_case for file names.
- Use meaningful names for variables, functions, and classes.
- Write clear and concise comments.
- Document your code using Doxygen-style comments.

### Documentation

Documentation is an important part of the project. Please make sure to document your code and update the documentation when making changes.

Documentation is written in Markdown and is located in the `docs` directory.

### Testing

Testing is an important part of the development process. Please make sure to write tests for your changes and run the existing tests to ensure they still pass.

We use Google Test for unit testing and integration testing.

## Project Structure

The project is structured as follows:

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

## Communication

### GitHub Issues

Use GitHub issues to report bugs, request features, or ask questions about the project.

### Pull Requests

Use pull requests to submit changes to the project. Make sure to follow the guidelines for creating pull requests.

### Community Forum

Join the community forum to discuss the project with other contributors and users.

### Email

Contact the project maintainers via email for private communication.

## Code of Conduct

We expect all contributors to follow our Code of Conduct. Please make sure to read and understand it before contributing.

## License

The project is licensed under the GPL-3.0 License. By contributing to the project, you agree to license your contributions under the same license.

## Acknowledgements

We would like to thank all contributors for their valuable contributions to the project.

## Conclusion

Thank you for your interest in contributing to the Degoogled Android Auto project! We look forward to your contributions and hope you enjoy being part of the community.