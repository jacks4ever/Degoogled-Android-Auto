#pragma once

#include <iostream>
#include <sstream>
#include <string>

namespace degoogled_aa {
namespace common {

/**
 * @brief Log level
 */
enum class LogLevel {
    trace,
    debug,
    info,
    warning,
    error,
    fatal
};

/**
 * @brief Logger class
 */
class Logger {
public:
    /**
     * @brief Construct a new Logger object
     * 
     * @param level Log level
     */
    Logger(LogLevel level)
        : level_(level) {
    }

    /**
     * @brief Destroy the Logger object
     */
    ~Logger() {
        std::cout << stream_.str() << std::endl;
    }

    /**
     * @brief Get the stream
     * 
     * @return std::ostringstream& Stream
     */
    std::ostringstream& stream() {
        return stream_;
    }

private:
    LogLevel level_;
    std::ostringstream stream_;
};

} // namespace common
} // namespace degoogled_aa

#define LOG(level) degoogled_aa::common::Logger(degoogled_aa::common::LogLevel::level).stream()