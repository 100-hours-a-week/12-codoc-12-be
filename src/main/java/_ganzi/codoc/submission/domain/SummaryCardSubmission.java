package _ganzi.codoc.submission.domain;

import _ganzi.codoc.problem.domain.SummaryCard;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "summary_card_submission")
@Entity
public class SummaryCardSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private SummaryCardAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "summary_card_id", nullable = false)
    private SummaryCard summaryCard;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    private SummaryCardSubmission(
            SummaryCardAttempt attempt, SummaryCard summaryCard, boolean correct, Instant submittedAt) {
        this.attempt = attempt;
        this.summaryCard = summaryCard;
        this.correct = correct;
        this.submittedAt = submittedAt;
    }

    public static SummaryCardSubmission create(
            SummaryCardAttempt attempt, SummaryCard summaryCard, boolean correct, Instant submittedAt) {
        return new SummaryCardSubmission(attempt, summaryCard, correct, submittedAt);
    }
}
