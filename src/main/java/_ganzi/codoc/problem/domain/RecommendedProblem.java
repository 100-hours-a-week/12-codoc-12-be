package _ganzi.codoc.problem.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "recommended_problem",
        indexes = {
            @Index(name = "idx_recommended_problem_user_done", columnList = "user_id,is_done"),
            @Index(name = "idx_recommended_problem_user_solved_at", columnList = "user_id,solved_at")
        })
@Entity
public class RecommendedProblem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "reason_msg", nullable = false, length = 500)
    private String reasonMsg;

    @Column(name = "recommended_at", nullable = false, updatable = false)
    private Instant recommendedAt;

    @Column(name = "solved_at")
    private Instant solvedAt;

    @Column(name = "is_done", nullable = false)
    private boolean isDone;

    private RecommendedProblem(User user, Problem problem, String reasonMsg, Instant recommendedAt) {
        this.user = user;
        this.problem = problem;
        this.reasonMsg = reasonMsg;
        this.recommendedAt = recommendedAt;
    }

    public static RecommendedProblem create(User user, Problem problem, String reasonMsg) {
        return new RecommendedProblem(user, problem, reasonMsg, Instant.now());
    }

    public void markDone(Instant solvedAt) {
        if (!this.isDone) {
            this.isDone = true;
            this.solvedAt = solvedAt;
        }
    }
}
