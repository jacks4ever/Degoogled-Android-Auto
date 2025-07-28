#pragma once

#include <string>

namespace degoogled_aa {
namespace error {

/**
 * @brief Error codes
 */
enum class ErrorCode {
    UNKNOWN = 0,
    NOT_STARTED = 1,
    ALREADY_STARTED = 2,
    NOT_FOUND = 3,
    INVALID_ARGUMENT = 4,
    TIMEOUT = 5,
    PERMISSION_DENIED = 6,
    INTERNAL_ERROR = 7
};

/**
 * @brief Error class
 */
class Error {
public:
    /**
     * @brief Construct a new Error object
     * 
     * @param code Error code
     * @param message Error message
     */
    Error(ErrorCode code, const std::string& message)
        : code_(code)
        , message_(message) {
    }

    /**
     * @brief Get the error code
     * 
     * @return ErrorCode Error code
     */
    ErrorCode code() const {
        return code_;
    }

    /**
     * @brief Get the error message
     * 
     * @return const std::string& Error message
     */
    const std::string& message() const {
        return message_;
    }

private:
    ErrorCode code_;
    std::string message_;
};

} // namespace error
} // namespace degoogled_aa