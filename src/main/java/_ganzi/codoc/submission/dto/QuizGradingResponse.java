package _ganzi.codoc.submission.dto;

import lombok.Builder;

@Builder
public record QuizGradingResponse(boolean result, Long attemptId) {

    public static QuizGradingResponse of(boolean result, Long attemptId) {
        return QuizGradingResponse.builder().result(result).attemptId(attemptId).build();
    }
}
