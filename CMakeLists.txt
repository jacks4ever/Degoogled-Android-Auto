cmake_minimum_required(VERSION 3.10)
project(DegoogledAndroidAuto VERSION 1.0.0 LANGUAGES CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Find required packages
find_package(Threads REQUIRED)
find_package(Boost COMPONENTS system REQUIRED)

# Add include directories
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${Boost_INCLUDE_DIRS}
)

# Define source files
file(GLOB_RECURSE SOURCES
    "src/protocol_handler/*.cpp"
)

# Create library
add_library(degoogled_aa SHARED ${SOURCES})

# Link libraries
target_link_libraries(degoogled_aa
    PRIVATE
    Threads::Threads
    ${Boost_LIBRARIES}
)

# Install
install(TARGETS degoogled_aa
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
)