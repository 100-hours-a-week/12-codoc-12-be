package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.domain.job.RecommendationJob;
import _ganzi.codoc.problem.domain.job.RecommendationJobStatus;
import _ganzi.codoc.problem.service.RecommendationScenario;
import java.time.Instant;

public record RecommendationJobResponse(
        String jobId,
        RecommendationScenario scenario,
        RecommendationJobStatus status,
        String errorCode,
        String errorMessage,
        Instant requestedAt,
        Instant respondedAt,
        Instant createdAt) {

    public static RecommendationJobResponse from(RecommendationJob job) {
        return new RecommendationJobResponse(
                job.getJobId(),
                job.getScenario(),
                job.getStatus(),
                job.getErrorCode(),
                job.getErrorMessage(),
                job.getRequestedAt(),
                job.getRespondedAt(),
                job.getCreatedAt());
    }
}
