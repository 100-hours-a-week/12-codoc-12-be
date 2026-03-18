package _ganzi.codoc.problem.mq;

import _ganzi.codoc.problem.domain.job.RecommendationJob;
import _ganzi.codoc.problem.repository.job.RecommendationJobRepository;
import _ganzi.codoc.problem.service.RecommendedProblemService;
import _ganzi.codoc.problem.service.recommend.dto.RecommendResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.recommend.mq",
        name = {"enabled", "response-consume-enabled"},
        havingValue = "true")
public class RecommendResponseConsumer {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";

    private final RecommendationJobRepository recommendationJobRepository;
    private final RecommendedProblemService recommendedProblemService;

    @Transactional
    @RabbitListener(
            queues = "${app.recommend.mq.response-queue}",
            containerFactory = "recommendRabbitListenerContainerFactory")
    public void consume(RecommendResponseMessage message) {
        if (message.jobId() == null || message.jobId().isBlank()) {
            log.warn("recommend response ignored. jobId is empty");
            return;
        }
        RecommendationJob job = recommendationJobRepository.findById(message.jobId()).orElse(null);
        if (job == null) {
            log.warn("recommend response ignored. job not found. jobId={}", message.jobId());
            return;
        }
        if (job.isTerminal()) {
            log.info(
                    "recommend response ignored. already terminal. jobId={}, status={}",
                    message.jobId(),
                    job.getStatus());
            return;
        }

        Instant respondedAt = message.respondedAt() == null ? Instant.now() : message.respondedAt();
        if (SUCCESS.equalsIgnoreCase(message.status())) {
            RecommendResponse result = message.result();
            if (result == null) {
                job.markFailed("INVALID_RESPONSE", "result is null", respondedAt);
                return;
            }
            recommendedProblemService.applyRecommendationsFromResponse(
                    job.getUserId(), job.getScenario(), result);
            job.markDone(respondedAt);
            return;
        }

        if (FAILED.equalsIgnoreCase(message.status())) {
            String errorCode = message.errorCode() == null ? "UNKNOWN" : message.errorCode();
            String errorMessage =
                    message.errorMessage() == null ? "recommend request failed" : message.errorMessage();
            job.markFailed(errorCode, errorMessage, respondedAt);
            return;
        }

        job.markFailed("INVALID_STATUS", "unsupported status: " + message.status(), respondedAt);
    }
}
