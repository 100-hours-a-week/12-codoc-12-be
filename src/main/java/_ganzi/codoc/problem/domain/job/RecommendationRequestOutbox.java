package _ganzi.codoc.problem.domain.job;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.infra.RecommendRequestPayloadConverter;
import _ganzi.codoc.problem.mq.RecommendRequestMessage;
import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recommendation_request_outbox")
@Entity
public class RecommendationRequestOutbox extends BaseTimeEntity {

    private static final int LAST_ERROR_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, length = 64, unique = true)
    private String jobId;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Convert(converter = RecommendRequestPayloadConverter.class)
    @Column(name = "payload", nullable = false, columnDefinition = "json")
    private RecommendRequest payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecommendationRequestOutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_error", length = LAST_ERROR_MAX_LENGTH)
    private String lastError;

    private RecommendationRequestOutbox(
            String jobId, Instant requestedAt, RecommendRequest payload, Instant nextAttemptAt) {
        this.jobId = jobId;
        this.requestedAt = requestedAt;
        this.payload = payload;
        this.status = RecommendationRequestOutboxStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = nextAttemptAt;
    }

    public static RecommendationRequestOutbox create(
            String jobId, Instant requestedAt, RecommendRequest payload, Instant now) {
        return new RecommendationRequestOutbox(jobId, requestedAt, payload, now);
    }

    public void markProcessing(Instant now) {
        this.status = RecommendationRequestOutboxStatus.PROCESSING;
        this.processingStartedAt = now;
    }

    public void markPublished(Instant now) {
        this.status = RecommendationRequestOutboxStatus.PUBLISHED;
        this.processingStartedAt = null;
        this.publishedAt = now;
        this.lastError = null;
    }

    public RecommendationRequestOutboxStatus markFailed(
            Instant now, String errorSummary, Duration retryDelay, int maxPublishAttempts) {
        this.attemptCount += 1;
        this.processingStartedAt = null;
        this.lastError = abbreviate(errorSummary);
        if (this.attemptCount >= maxPublishAttempts) {
            this.status = RecommendationRequestOutboxStatus.DEAD;
            this.nextAttemptAt = now;
            return this.status;
        }
        this.status = RecommendationRequestOutboxStatus.FAILED;
        this.nextAttemptAt = now.plus(retryDelay);
        return this.status;
    }

    public RecommendRequestMessage toMessage() {
        return new RecommendRequestMessage(jobId, requestedAt, payload);
    }

    private String abbreviate(String errorSummary) {
        if (!StringUtils.hasText(errorSummary)) {
            return null;
        }
        return errorSummary.length() <= LAST_ERROR_MAX_LENGTH
                ? errorSummary
                : errorSummary.substring(0, LAST_ERROR_MAX_LENGTH);
    }
}
