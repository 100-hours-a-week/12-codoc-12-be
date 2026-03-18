package _ganzi.codoc.problem.domain.job;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.service.RecommendationScenario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "recommendation_job")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationJob extends BaseTimeEntity {

    @Id
    @Column(name = "job_id", nullable = false, length = 64)
    private String jobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario", nullable = false, length = 32)
    private RecommendationScenario scenario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RecommendationJobStatus status;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    private RecommendationJob(
            String jobId,
            Long userId,
            RecommendationScenario scenario,
            RecommendationJobStatus status,
            Instant requestedAt) {
        this.jobId = jobId;
        this.userId = userId;
        this.scenario = scenario;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public static RecommendationJob publish(
            String jobId, Long userId, RecommendationScenario scenario, Instant requestedAt) {
        return new RecommendationJob(
                jobId, userId, scenario, RecommendationJobStatus.PUBLISHED, requestedAt);
    }

    public void markDone(Instant respondedAt) {
        this.status = RecommendationJobStatus.DONE;
        this.respondedAt = respondedAt;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void markFailed(String errorCode, String errorMessage, Instant respondedAt) {
        this.status = RecommendationJobStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.respondedAt = respondedAt;
    }

    public void markPublishFailed(String errorCode, String errorMessage) {
        this.status = RecommendationJobStatus.FAILED_PUBLISH;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isTerminal() {
        return status == RecommendationJobStatus.DONE
                || status == RecommendationJobStatus.FAILED
                || status == RecommendationJobStatus.FAILED_PUBLISH;
    }
}
