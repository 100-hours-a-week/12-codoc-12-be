package _ganzi.codoc.submission.dto;

import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import java.time.Instant;
import lombok.Builder;

@Builder
public record ProblemSubmissionResponse(
        int correctCount,
        ProblemSolvingStatus nextStatus,
        boolean xpGranted,
        Instant createdAt,
        Instant closedAt) {

    public static ProblemSubmissionResponse of(
            int correctCount,
            ProblemSolvingStatus nextStatus,
            boolean xpGranted,
            Instant createdAt,
            Instant closedAt) {
        return ProblemSubmissionResponse.builder()
                .correctCount(correctCount)
                .nextStatus(nextStatus)
                .xpGranted(xpGranted)
                .createdAt(createdAt)
                .closedAt(closedAt)
                .build();
    }
}
