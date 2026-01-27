package _ganzi.codoc.submission.dto;

import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import lombok.Builder;

@Builder
public record ProblemSubmissionResponse(
        int correctCount, ProblemSolvingStatus nextStatus, boolean xpGranted) {

    public static ProblemSubmissionResponse of(
            int correctCount, ProblemSolvingStatus nextStatus, boolean xpGranted) {
        return ProblemSubmissionResponse.builder()
                .correctCount(correctCount)
                .nextStatus(nextStatus)
                .xpGranted(xpGranted)
                .build();
    }
}
