package _ganzi.codoc.surprise.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "surprise_quiz_submission")
@Entity
public class SurpriseQuizSubmission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private SurpriseEvent event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "elapsed_millis", nullable = false)
    private long elapsedMillis;

    @Column(name = "rank_no")
    private Integer rankNo;

    @Column(name = "earned_xp")
    private Integer earnedXp;

    private SurpriseQuizSubmission(
            SurpriseEvent event,
            User user,
            boolean correct,
            Instant submittedAt,
            long elapsedMillis,
            Integer rankNo) {
        this.event = event;
        this.user = user;
        this.correct = correct;
        this.submittedAt = submittedAt;
        this.elapsedMillis = elapsedMillis;
        this.rankNo = rankNo;
    }

    public static SurpriseQuizSubmission submit(
            SurpriseEvent event,
            User user,
            boolean correct,
            Instant submittedAt,
            long elapsedMillis,
            Integer rankNo) {
        return new SurpriseQuizSubmission(event, user, correct, submittedAt, elapsedMillis, rankNo);
    }

    public void assignRank(Integer rankNo) {
        this.rankNo = rankNo;
    }

    public void assignEarnedXp(int earnedXp) {
        this.earnedXp = earnedXp;
    }
}
