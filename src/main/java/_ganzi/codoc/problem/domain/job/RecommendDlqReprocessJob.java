package _ganzi.codoc.problem.domain.job;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "recommend_dlq_reprocess_job")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendDlqReprocessJob extends BaseTimeEntity {

    private static final int LAST_ERROR_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private RecommendDlqReprocessRequestType requestType;

    @Column(name = "target_job_id", length = 64)
    private String targetJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecommendDlqReprocessJobStatus status;

    @Column(name = "requested_limit", nullable = false)
    private int requestedLimit;

    @Column(name = "success_count", nullable = false)
    private int successCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    @Column(name = "failed_job_ids_json", columnDefinition = "json")
    private String failedJobIdsJson;

    @Column(name = "last_error", length = LAST_ERROR_MAX_LENGTH)
    private String lastError;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    private RecommendDlqReprocessJob(
            RecommendDlqReprocessRequestType requestType, String targetJobId, int requestedLimit) {
        this.requestType = requestType;
        this.targetJobId = targetJobId;
        this.status = RecommendDlqReprocessJobStatus.QUEUED;
        this.requestedLimit = requestedLimit;
        this.successCount = 0;
        this.failedCount = 0;
        this.skippedCount = 0;
    }

    public static RecommendDlqReprocessJob createBatch(int requestedLimit) {
        return new RecommendDlqReprocessJob(
                RecommendDlqReprocessRequestType.BATCH, null, requestedLimit);
    }

    public static RecommendDlqReprocessJob createSingle(String targetJobId) {
        return new RecommendDlqReprocessJob(RecommendDlqReprocessRequestType.SINGLE, targetJobId, 1);
    }

    public void start(Instant now) {
        this.status = RecommendDlqReprocessJobStatus.RUNNING;
        this.startedAt = now;
    }

    public void finishSuccess(
            int successCount, int failedCount, int skippedCount, String failedJobIdsJson, Instant now) {
        this.status = RecommendDlqReprocessJobStatus.SUCCEEDED;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.skippedCount = skippedCount;
        this.failedJobIdsJson = failedJobIdsJson;
        this.finishedAt = now;
        this.lastError = null;
    }

    public void finishFailed(
            int successCount,
            int failedCount,
            int skippedCount,
            String failedJobIdsJson,
            String lastError,
            Instant now) {
        this.status = RecommendDlqReprocessJobStatus.FAILED;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.skippedCount = skippedCount;
        this.failedJobIdsJson = failedJobIdsJson;
        this.finishedAt = now;
        this.lastError = abbreviate(lastError);
    }

    public boolean isRunning() {
        return status == RecommendDlqReprocessJobStatus.QUEUED
                || status == RecommendDlqReprocessJobStatus.RUNNING;
    }

    private String abbreviate(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        return message.length() <= LAST_ERROR_MAX_LENGTH
                ? message
                : message.substring(0, LAST_ERROR_MAX_LENGTH);
    }
}
