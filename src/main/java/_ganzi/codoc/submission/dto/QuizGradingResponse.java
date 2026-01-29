package _ganzi.codoc.submission.dto;

import lombok.Builder;

@Builder
public record QuizGradingResponse(boolean result, Long attemptId, String explanation) {

    public static QuizGradingResponse of(boolean result, Long attemptId, String explanation) {
        return QuizGradingResponse.builder()
                .result(result)
                .attemptId(attemptId)
                .explanation(explanation)
                .build();
    }
}
