package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.config.RecommendOutboxProperties;
import _ganzi.codoc.problem.domain.job.RecommendationRequestOutboxStatus;
import _ganzi.codoc.problem.mq.RecommendRequestPublisher;
import _ganzi.codoc.problem.service.RecommendationRequestOutboxService.RecommendationRequestOutboxCandidate;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendationRequestOutboxPublishService {

    private final RecommendationRequestOutboxService recommendationRequestOutboxService;
    private final RecommendOutboxProperties recommendOutboxProperties;
    private final ObjectProvider<RecommendRequestPublisher> recommendRequestPublisherProvider;

    public void publishPendingRequests() {
        RecommendRequestPublisher recommendRequestPublisher =
                recommendRequestPublisherProvider.getIfAvailable();
        if (recommendRequestPublisher == null) {
            return;
        }

        int maxBatchesPerRun = Math.max(1, recommendOutboxProperties.maxBatchesPerRun());

        for (int batchNumber = 0; batchNumber < maxBatchesPerRun; batchNumber++) {
            List<RecommendationRequestOutboxCandidate> candidates =
                    recommendationRequestOutboxService.claimNextBatch();
            if (candidates.isEmpty()) {
                return;
            }

            candidates.forEach(candidate -> publishSingle(recommendRequestPublisher, candidate));
            if (candidates.size() < Math.max(1, recommendOutboxProperties.batchSize())) {
                return;
            }
        }
    }

    private void publishSingle(
            RecommendRequestPublisher recommendRequestPublisher,
            RecommendationRequestOutboxCandidate candidate) {
        try {
            recommendRequestPublisher.publish(candidate.message());
            recommendationRequestOutboxService.markPublished(candidate.jobId());
        } catch (Exception exception) {
            Duration retryDelay = resolveRetryDelay(candidate.attemptCount() + 1);
            RecommendationRequestOutboxStatus status =
                    recommendationRequestOutboxService.markFailed(candidate.jobId(), exception, retryDelay);
            if (status == RecommendationRequestOutboxStatus.DEAD) {
                log.error(
                        "recommend request outbox moved to dead state. jobId={}", candidate.jobId(), exception);
                return;
            }
            log.warn(
                    "recommend request outbox publish failed. jobId={}, attempt={}",
                    candidate.jobId(),
                    candidate.attemptCount() + 1,
                    exception);
        }
    }

    private Duration resolveRetryDelay(int attempt) {
        Duration initialDelay = recommendOutboxProperties.retryInitialInterval();
        Duration maxDelay = recommendOutboxProperties.retryMaxInterval();
        double multiplier =
                recommendOutboxProperties.retryMultiplier() <= 0.0
                        ? 1.0
                        : recommendOutboxProperties.retryMultiplier();
        long delayMillis = Math.max(0L, initialDelay.toMillis());

        for (int currentAttempt = 1; currentAttempt < attempt; currentAttempt++) {
            delayMillis = Math.min(maxDelay.toMillis(), Math.round(delayMillis * multiplier));
        }

        return Duration.ofMillis(delayMillis);
    }
}
