package _ganzi.codoc.ai.dto;

import _ganzi.codoc.chatbot.enums.ChatbotParagraphType;
import lombok.Builder;

@Builder
public record AiServerChatbotSendRequest(
        Long userId,
        Long problemId,
        Long runId,
        String userMessage,
        ChatbotParagraphType paragraphType) {

    public static AiServerChatbotSendRequest of(
            Long userId,
            Long problemId,
            Long runId,
            String userMessage,
            ChatbotParagraphType paragraphType) {

        return AiServerChatbotSendRequest.builder()
                .userId(userId)
                .problemId(problemId)
                .runId(runId)
                .userMessage(userMessage)
                .paragraphType(paragraphType)
                .build();
    }
}
