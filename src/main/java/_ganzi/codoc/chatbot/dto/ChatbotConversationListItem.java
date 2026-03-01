package _ganzi.codoc.chatbot.dto;

import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import _ganzi.codoc.chatbot.enums.ChatbotConversationStatus;

public record ChatbotConversationListItem(
        Long conversationId, String userMessage, String aiMessage, ChatbotConversationStatus status) {

    public static ChatbotConversationListItem from(ChatbotConversation conversation) {
        return new ChatbotConversationListItem(
                conversation.getId(),
                conversation.getUserMessage(),
                conversation.getAiMessage(),
                conversation.getStatus());
    }
}
