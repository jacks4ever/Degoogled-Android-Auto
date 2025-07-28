#pragma once

#include <memory>
#include <functional>
#include "Error.hpp"

namespace degoogled_aa {
namespace common {

/**
 * @brief Promise template class
 * 
 * @tparam T Type of the value to be resolved
 */
template<typename T>
class Promise {
public:
    typedef std::shared_ptr<Promise<T>> Pointer;
    typedef std::function<void(const T&)> ResolveCallback;
    typedef std::function<void(const error::Error&)> RejectCallback;

    /**
     * @brief Construct a new Promise object
     */
    Promise() = default;

    /**
     * @brief Destroy the Promise object
     */
    virtual ~Promise() = default;

    /**
     * @brief Set the resolve callback
     * 
     * @param callback Callback to be called when the promise is resolved
     * @return Promise<T>& Reference to this promise
     */
    Promise<T>& then(ResolveCallback callback) {
        resolveCallback_ = callback;
        return *this;
    }

    /**
     * @brief Set the reject callback
     * 
     * @param callback Callback to be called when the promise is rejected
     * @return Promise<T>& Reference to this promise
     */
    Promise<T>& catch_(RejectCallback callback) {
        rejectCallback_ = callback;
        return *this;
    }

    /**
     * @brief Resolve the promise
     * 
     * @param value Value to resolve the promise with
     */
    void resolve(const T& value) {
        if (resolveCallback_) {
            resolveCallback_(value);
        }
    }

    /**
     * @brief Reject the promise
     * 
     * @param error Error to reject the promise with
     */
    void reject(const error::Error& error) {
        if (rejectCallback_) {
            rejectCallback_(error);
        }
    }

private:
    ResolveCallback resolveCallback_;
    RejectCallback rejectCallback_;
};

/**
 * @brief Void promise
 */
typedef Promise<void*> VoidPromise;

} // namespace common
} // namespace degoogled_aa