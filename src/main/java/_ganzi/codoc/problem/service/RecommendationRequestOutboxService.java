package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.config.RecommendOutboxProperties;
import _ganzi.codoc.problem.domain.job.RecommendationJob;
import _ganzi.codoc.problem.domain.job.RecommendationRequestOutbox;
import _ganzi.codoc.problem.domain.job.RecommendationRequestOutboxStatus;
import _ganzi.codoc.problem.mq.RecommendRequestMessage;
import _ganzi.codoc.problem.repository.job.RecommendationJobRepository;
import _ganzi.codoc.problem.repository.job.RecommendationRequestOutboxRepository;
import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
public class RecommendationRequestOutboxService {

    private final RecommendationRequestOutboxRepository recommendationRequestOutboxRepository;
    private final RecommendationJobRepository recommendationJobRepository;
    private final RecommendOutboxProperties recommendOutboxProperties;

    @Transactional
    public void enqueue(String jobId, Instant requestedAt, RecommendRequest payload) {
        recommendationRequestOutboxRepository.save(
                RecommendationRequestOutbox.create(jobId, requestedAt, payload, Instant.now()));
    }

    public Duration initialRetryDelay() {
        return recommendOutboxProperties.retryInitialInterval();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RecommendationRequestOutboxCandidate> claimNextBatch() {
        Instant now = Instant.now();
        int batchSize = Math.max(1, recommendOutboxProperties.batchSize());
        Instant staleThreshold = now.minus(recommendOutboxProperties.processingTimeout());

        List<RecommendationRequestOutbox> outboxes =
                recommendationRequestOutboxRepository.lockNextPublishBatch(now, staleThreshold, batchSize);
        if (outboxes.isEmpty()) {
            return List.of();
        }

        outboxes.forEach(outbox -> outbox.markProcessing(now));

        return outboxes.stream().map(RecommendationRequestOutboxCandidate::from).toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(String jobId) {
        recommendationRequestOutboxRepository
                .findByJobId(jobId)
                .ifPresent(outbox -> outbox.markPublished(Instant.now()));
        recommendationJobRepository.findById(jobId).ifPresent(RecommendationJob::markPublished);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RecommendationRequestOutboxStatus markFailed(
            String jobId, Exception exception, Duration retryDelay) {
        RecommendationRequestOutboxStatus status =
                recommendationRequestOutboxRepository
                        .findByJobId(jobId)
                        .map(
                                outbox ->
                                        outbox.markFailed(
                                                Instant.now(),
                                                summarizeException(exception),
                                                retryDelay,
                                                Math.max(1, recommendOutboxProperties.maxPublishAttempts())))
                        .orElse(RecommendationRequestOutboxStatus.DEAD);
        if (status == RecommendationRequestOutboxStatus.DEAD) {
            recommendationJobRepository
                    .findById(jobId)
                    .ifPresent(job -> job.markPublishFailed("PUBLISH_FAILED", summarizeException(exception)));
        }
        return status;
    }

    private String summarizeException(Exception exception) {
        if (exception == null) {
            return null;
        }
        String message = exception.getMessage();
        if (!StringUtils.hasText(message)) {
            return exception.getClass().getSimpleName();
        }
        return exception.getClass().getSimpleName() + ": " + message;
    }

    public record RecommendationRequestOutboxCandidate(
            String jobId, RecommendRequestMessage message, int attemptCount) {

        private static RecommendationRequestOutboxCandidate from(RecommendationRequestOutbox outbox) {
            return new RecommendationRequestOutboxCandidate(
                    outbox.getJobId(), outbox.toMessage(), outbox.getAttemptCount());
        }
    }
}
