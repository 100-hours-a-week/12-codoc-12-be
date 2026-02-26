package _ganzi.codoc.submission.domain;

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
@Table(name = "summary_card_attempt")
@Entity
public class SummaryCardAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_session_id", nullable = false)
    private ProblemSession problemSession;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    private SummaryCardAttempt(ProblemSession problemSession, Instant createdAt) {
        this.problemSession = problemSession;
        this.createdAt = createdAt;
    }

    public static SummaryCardAttempt create(ProblemSession problemSession, Instant createdAt) {
        return new SummaryCardAttempt(problemSession, createdAt);
    }

    public void complete(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
