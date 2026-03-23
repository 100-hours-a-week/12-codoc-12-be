package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.domain.job.RecommendationJob;
import _ganzi.codoc.problem.mq.RecommendResponseMessage;
import _ganzi.codoc.problem.repository.job.RecommendationJobRepository;
import _ganzi.codoc.problem.service.recommend.dto.RecommendResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendResponseProcessingService {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";

    private final RecommendationJobRepository recommendationJobRepository;
    private final RecommendedProblemService recommendedProblemService;

    @Transactional
    public RecommendResponseProcessResult process(RecommendResponseMessage message) {
        if (message.jobId() == null || message.jobId().isBlank()) {
            log.warn("recommend response ignored. jobId is empty");
            return RecommendResponseProcessResult.skipped("EMPTY_JOB_ID");
        }

        RecommendationJob job = recommendationJobRepository.findById(message.jobId()).orElse(null);
        if (job == null) {
            log.warn("recommend response ignored. job not found. jobId={}", message.jobId());
            return RecommendResponseProcessResult.skipped("JOB_NOT_FOUND");
        }

        if (job.isTerminal()) {
            log.info(
                    "recommend response ignored. already terminal. jobId={}, status={}",
                    message.jobId(),
                    job.getStatus());
            return RecommendResponseProcessResult.skipped("ALREADY_TERMINAL");
        }

        Instant respondedAt = message.respondedAt() == null ? Instant.now() : message.respondedAt();
        if (SUCCESS.equalsIgnoreCase(message.status())) {
            RecommendResponse result = message.result();
            if (result == null) {
                job.markFailed("INVALID_RESPONSE", "result is null", respondedAt);
                return RecommendResponseProcessResult.failed(message.jobId(), "INVALID_RESPONSE");
            }
            recommendedProblemService.applyRecommendationsFromResponse(
                    job.getUserId(), job.getScenario(), result);
            job.markDone(respondedAt);
            return RecommendResponseProcessResult.success(message.jobId());
        }

        if (FAILED.equalsIgnoreCase(message.status())) {
            String errorCode = message.errorCode() == null ? "UNKNOWN" : message.errorCode();
            String errorMessage =
                    message.errorMessage() == null ? "recommend request failed" : message.errorMessage();
            job.markFailed(errorCode, errorMessage, respondedAt);
            return RecommendResponseProcessResult.failed(message.jobId(), errorCode);
        }

        job.markFailed("INVALID_STATUS", "unsupported status: " + message.status(), respondedAt);
        return RecommendResponseProcessResult.failed(message.jobId(), "INVALID_STATUS");
    }

    public record RecommendResponseProcessResult(String jobId, ProcessType type, String reasonCode) {

        public static RecommendResponseProcessResult success(String jobId) {
            return new RecommendResponseProcessResult(jobId, ProcessType.SUCCESS, null);
        }

        public static RecommendResponseProcessResult failed(String jobId, String reasonCode) {
            return new RecommendResponseProcessResult(jobId, ProcessType.FAILED, reasonCode);
        }

        public static RecommendResponseProcessResult skipped(String reasonCode) {
            return new RecommendResponseProcessResult(null, ProcessType.SKIPPED, reasonCode);
        }

        public boolean isSuccess() {
            return type == ProcessType.SUCCESS;
        }

        public boolean isSkipped() {
            return type == ProcessType.SKIPPED;
        }
    }

    public enum ProcessType {
        SUCCESS,
        FAILED,
        SKIPPED
    }
}
