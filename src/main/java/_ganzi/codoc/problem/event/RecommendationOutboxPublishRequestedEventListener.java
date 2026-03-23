package _ganzi.codoc.problem.event;

import _ganzi.codoc.problem.mq.RecommendRequestPublisher;
import _ganzi.codoc.problem.service.RecommendationRequestOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class RecommendationOutboxPublishRequestedEventListener {

    private final ObjectProvider<RecommendRequestPublisher> recommendRequestPublisherProvider;
    private final RecommendationRequestOutboxService recommendationRequestOutboxService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RecommendationOutboxPublishRequestedEvent event) {
        RecommendRequestPublisher publisher = recommendRequestPublisherProvider.getIfAvailable();
        if (publisher == null) {
            return;
        }
        try {
            publisher.publish(event.jobId(), event.request(), event.requestedAt());
            recommendationRequestOutboxService.markPublished(event.jobId());
        } catch (Exception exception) {
            recommendationRequestOutboxService.markFailed(
                    event.jobId(), exception, recommendationRequestOutboxService.initialRetryDelay());
            log.warn("recommend request after-commit publish failed. jobId={}", event.jobId(), exception);
        }
    }
}
