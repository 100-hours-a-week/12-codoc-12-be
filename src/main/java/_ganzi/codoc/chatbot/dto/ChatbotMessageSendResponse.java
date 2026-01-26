package _ganzi.codoc.chatbot.dto;

import _ganzi.codoc.ai.enums.ChatbotStatus;
import lombok.Builder;

@Builder
public record ChatbotMessageSendResponse(Long conversationId, ChatbotStatus status) {

    public static ChatbotMessageSendResponse of(Long conversationId, ChatbotStatus status) {
        return ChatbotMessageSendResponse.builder()
                .conversationId(conversationId)
                .status(status)
                .build();
    }
}
