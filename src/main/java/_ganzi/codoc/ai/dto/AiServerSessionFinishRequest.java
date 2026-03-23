package _ganzi.codoc.ai.dto;

import _ganzi.codoc.user.enums.InitLevel;
import lombok.Builder;

@Builder
public record AiServerSessionFinishRequest(
        String sessionId, Long userId, Long problemId, InitLevel userLevel) {

    public static AiServerSessionFinishRequest of(
            String sessionId, Long userId, Long problemId, InitLevel userLevel) {
        return AiServerSessionFinishRequest.builder()
                .sessionId(sessionId)
                .userId(userId)
                .problemId(problemId)
                .userLevel(userLevel)
                .build();
    }
}
