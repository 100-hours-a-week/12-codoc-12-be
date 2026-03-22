package _ganzi.codoc.ai.dto;

import _ganzi.codoc.chatbot.enums.ChatbotMessageType;
import _ganzi.codoc.chatbot.enums.ChatbotParagraphType;
import _ganzi.codoc.user.enums.InitLevel;
import lombok.Builder;

@Builder
public record AiServerChatbotSendRequest(
        Long userId,
        Long problemId,
        Long runId,
        String userMessage,
        InitLevel userLevel,
        String sessionId,
        ChatbotParagraphType paragraphType,
        ChatbotMessageType messageType) {

    public static AiServerChatbotSendRequest of(
            Long userId,
            Long problemId,
            Long runId,
            String userMessage,
            InitLevel userLevel,
            String sessionId,
            ChatbotParagraphType paragraphType,
            ChatbotMessageType messageType) {

        return AiServerChatbotSendRequest.builder()
                .userId(userId)
                .problemId(problemId)
                .runId(runId)
                .userMessage(userMessage)
                .userLevel(userLevel)
                .sessionId(sessionId)
                .paragraphType(paragraphType)
                .messageType(messageType)
                .build();
    }
}
