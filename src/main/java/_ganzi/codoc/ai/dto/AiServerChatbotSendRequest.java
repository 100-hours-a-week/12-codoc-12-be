package _ganzi.codoc.ai.dto;

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
        ChatbotParagraphType paragraphType) {

    public static AiServerChatbotSendRequest of(
            Long userId,
            Long problemId,
            Long runId,
            String userMessage,
            InitLevel userLevel,
            ChatbotParagraphType paragraphType) {

        return AiServerChatbotSendRequest.builder()
                .userId(userId)
                .problemId(problemId)
                .runId(runId)
                .userMessage(userMessage)
                .userLevel(userLevel)
                .paragraphType(paragraphType)
                .build();
    }
}
