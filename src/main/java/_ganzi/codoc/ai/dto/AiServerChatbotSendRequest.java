package _ganzi.codoc.ai.dto;

import _ganzi.codoc.problem.enums.ParagraphType;
import lombok.Builder;

@Builder
public record AiServerChatbotSendRequest(
        Long userId, Long problemId, Long runId, String userMessage, ParagraphType currentNode) {

    public static AiServerChatbotSendRequest of(
            Long userId, Long problemId, Long runId, String userMessage, ParagraphType currentNode) {

        return AiServerChatbotSendRequest.builder()
                .userId(userId)
                .problemId(problemId)
                .runId(runId)
                .userMessage(userMessage)
                .currentNode(currentNode)
                .build();
    }
}
