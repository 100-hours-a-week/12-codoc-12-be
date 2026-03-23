package _ganzi.codoc.problem.event;

import _ganzi.codoc.problem.mq.RecommendRequestPublisher;
import _ganzi.codoc.problem.service.RecommendationJobStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class RecommendationPublishRequestedEventListener {

    private final ObjectProvider<RecommendRequestPublisher> recommendRequestPublisherProvider;
    private final RecommendationJobStatusService recommendationJobStatusService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RecommendationPublishRequestedEvent event) {
        RecommendRequestPublisher publisher = recommendRequestPublisherProvider.getIfAvailable();
        if (publisher == null) {
            recommendationJobStatusService.markPublishFailed(
                    event.jobId(), "RecommendRequestPublisher unavailable");
            log.warn(
                    "recommend after-commit publish skipped. publisher unavailable. jobId={}", event.jobId());
            return;
        }

        try {
            publisher.publish(event.jobId(), event.request(), event.requestedAt());
            recommendationJobStatusService.markPublished(event.jobId());
        } catch (Exception exception) {
            recommendationJobStatusService.markPublishFailed(event.jobId(), exception.getMessage());
            log.warn("recommend after-commit publish failed. jobId={}", event.jobId(), exception);
        }
    }
}
