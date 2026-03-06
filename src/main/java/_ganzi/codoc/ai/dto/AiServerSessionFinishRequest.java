package _ganzi.codoc.ai.dto;

import lombok.Builder;

@Builder
public record AiServerSessionFinishRequest(String sessionId, Long userId, Long problemId) {

    public static AiServerSessionFinishRequest of(String sessionId, Long userId, Long problemId) {
        return AiServerSessionFinishRequest.builder()
                .sessionId(sessionId)
                .userId(userId)
                .problemId(problemId)
                .build();
    }
}
