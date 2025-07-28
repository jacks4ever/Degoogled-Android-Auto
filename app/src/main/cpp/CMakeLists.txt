cmake_minimum_required(VERSION 3.10.2)

project("degoogledandroidauto")

# Include the protocol handler source files
file(GLOB_RECURSE PROTOCOL_HANDLER_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/../../../../src/protocol_handler/*.cpp"
)

# If no protocol handler sources are found, create an empty list to avoid build errors
if(NOT PROTOCOL_HANDLER_SOURCES)
    set(PROTOCOL_HANDLER_SOURCES "")
    message(WARNING "No protocol handler source files found. Building with native-lib.cpp only.")
endif()

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
    ${CMAKE_CURRENT_SOURCE_DIR}/../../../../src
)

# Set C++ standard explicitly
set_target_properties(native-lib PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
)