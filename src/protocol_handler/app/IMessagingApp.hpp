#pragma once

#include <memory>
#include <string>
#include <vector>
#include <ctime>
#include "../common/Promise.hpp"
#include "../common/Error.hpp"

namespace degoogled_aa {
namespace app {

/**
 * @brief Conversation data structure
 */
struct Conversation {
    std::string id;
    std::string name;
    std::string lastMessage;
    std::time_t timestamp;
    int unreadCount;
    bool isGroup;
};

/**
 * @brief Message data structure
 */
struct Message {
    std::string id;
    std::string conversationId;
    std::string senderId;
    std::string senderName;
    std::string content;
    std::time_t timestamp;
    bool isRead;
    bool isOutgoing;
};

/**
 * @brief Attachment data structure
 */
struct Attachment {
    std::string id;
    std::string name;
    std::string mimeType;
    std::string uri;
    size_t size;
};

/**
 * @brief Interface for messaging applications
 */
class IMessagingApp {
public:
    typedef std::shared_ptr<IMessagingApp> Pointer;
    typedef common::Promise<std::vector<Conversation>> ConversationsPromise;
    typedef common::Promise<std::vector<Message>> MessagesPromise;

    /**
     * @brief Destroy the IMessagingApp object
     */
    virtual ~IMessagingApp() = default;

    /**
     * @brief Start the messaging app
     */
    virtual void start() = 0;

    /**
     * @brief Stop the messaging app
     */
    virtual void stop() = 0;

    /**
     * @brief Get the conversations
     * 
     * @param promise Promise to fulfill with the conversations
     */
    virtual void getConversations(ConversationsPromise::Pointer promise) = 0;

    /**
     * @brief Get the messages for a conversation
     * 
     * @param conversationId Conversation ID
     * @param limit Maximum number of messages to return
     * @param offset Offset to start from
     * @param promise Promise to fulfill with the messages
     */
    virtual void getMessages(const std::string& conversationId, int limit, int offset, MessagesPromise::Pointer promise) = 0;

    /**
     * @brief Send a message
     * 
     * @param conversationId Conversation ID
     * @param content Message content
     * @param attachments Message attachments
     * @param promise Promise to fulfill when the message is sent
     */
    virtual void sendMessage(const std::string& conversationId, const std::string& content, const std::vector<Attachment>& attachments, common::VoidPromise::Pointer promise) = 0;

    /**
     * @brief Mark a conversation as read
     * 
     * @param conversationId Conversation ID
     * @param promise Promise to fulfill when the conversation is marked as read
     */
    virtual void markConversationAsRead(const std::string& conversationId, common::VoidPromise::Pointer promise) = 0;

    /**
     * @brief Create a new conversation
     * 
     * @param recipients Recipients
     * @param promise Promise to fulfill with the new conversation ID
     */
    virtual void createConversation(const std::vector<std::string>& recipients, common::Promise<std::string>::Pointer promise) = 0;
};

} // namespace app
} // namespace degoogled_aa