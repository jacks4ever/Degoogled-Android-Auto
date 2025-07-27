cmake_minimum_required(VERSION 3.10.2)

project("degoogledandroidauto")

# Include the protocol handler source files
file(GLOB_RECURSE PROTOCOL_HANDLER_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/../../../src/protocol_handler/*.cpp"
)

# Create the native library
add_library(
    native-lib
    SHARED
    native-lib.cpp
    ${PROTOCOL_HANDLER_SOURCES}
)

# Find the required libraries
find_library(log-lib log)

# Link the libraries
target_link_libraries(
    native-lib
    ${log-lib}
)

# Include directories
target_include_directories(
    native-lib
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/../../../src
)