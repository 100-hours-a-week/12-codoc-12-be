package _ganzi.codoc.submission.dto;

import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import lombok.Builder;

@Builder
public record ProblemResultEvaluationResponse(
        int correctCount, ProblemSolvingStatus nextStatus, boolean xpGranted) {

    public static ProblemResultEvaluationResponse of(
            int correctCount, ProblemSolvingStatus nextStatus, boolean xpGranted) {
        return ProblemResultEvaluationResponse.builder()
                .correctCount(correctCount)
                .nextStatus(nextStatus)
                .xpGranted(xpGranted)
                .build();
    }
}
