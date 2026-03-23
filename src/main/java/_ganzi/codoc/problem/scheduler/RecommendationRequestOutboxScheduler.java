package _ganzi.codoc.problem.scheduler;

import _ganzi.codoc.problem.service.RecommendationRequestOutboxPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RecommendationRequestOutboxScheduler {

    private final RecommendationRequestOutboxPublishService recommendationRequestOutboxPublishService;

    @Scheduled(fixedDelayString = "${app.recommend.outbox.publish-fixed-delay}")
    public void publishPendingRequests() {
        recommendationRequestOutboxPublishService.publishPendingRequests();
    }
}
