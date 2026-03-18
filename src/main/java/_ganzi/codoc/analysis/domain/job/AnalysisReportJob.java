package _ganzi.codoc.analysis.domain.job;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "analysis_report_job")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisReportJob extends BaseTimeEntity {

    @Id
    @Column(name = "job_id", nullable = false, length = 64)
    private String jobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AnalysisReportJobStatus status;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    private AnalysisReportJob(
            String jobId,
            Long userId,
            AnalysisReportJobStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            Instant requestedAt) {
        this.jobId = jobId;
        this.userId = userId;
        this.status = status;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.requestedAt = requestedAt;
    }

    public static AnalysisReportJob publish(
            String jobId, Long userId, LocalDate periodStart, LocalDate periodEnd, Instant requestedAt) {
        return new AnalysisReportJob(
                jobId, userId, AnalysisReportJobStatus.PUBLISHED, periodStart, periodEnd, requestedAt);
    }

    public void markDone(Instant respondedAt) {
        this.status = AnalysisReportJobStatus.DONE;
        this.respondedAt = respondedAt;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void markFailed(String errorCode, String errorMessage, Instant respondedAt) {
        this.status = AnalysisReportJobStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.respondedAt = respondedAt;
    }

    public void markPublishFailed(String errorCode, String errorMessage) {
        this.status = AnalysisReportJobStatus.FAILED_PUBLISH;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isTerminal() {
        return status == AnalysisReportJobStatus.DONE
                || status == AnalysisReportJobStatus.FAILED
                || status == AnalysisReportJobStatus.FAILED_PUBLISH;
    }
}
