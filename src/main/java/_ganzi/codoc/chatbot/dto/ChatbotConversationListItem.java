package _ganzi.codoc.chatbot.dto;

import _ganzi.codoc.chatbot.domain.ChatbotConversation;

public record ChatbotConversationListItem(
        Long conversationId, String userMessage, String aiMessage) {

    public static ChatbotConversationListItem from(ChatbotConversation conversation) {
        return new ChatbotConversationListItem(
                conversation.getId(), conversation.getUserMessage(), conversation.getAiMessage());
    }
}
