package _ganzi.codoc.analysis.domain;

import _ganzi.codoc.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "analysis_report")
@Entity
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "report_json", nullable = false, columnDefinition = "json")
    private String reportJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    private AnalysisReport(
            User user, LocalDate periodStart, LocalDate periodEnd, String reportJson, Instant createdAt) {
        this.user = user;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.reportJson = reportJson;
        this.createdAt = createdAt;
    }

    public static AnalysisReport create(
            User user, LocalDate periodStart, LocalDate periodEnd, String reportJson, Instant createdAt) {
        return new AnalysisReport(user, periodStart, periodEnd, reportJson, createdAt);
    }
}
