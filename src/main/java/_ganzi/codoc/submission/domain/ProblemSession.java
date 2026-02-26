package _ganzi.codoc.submission.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.submission.enums.ProblemSessionStatus;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "problem_session",
        indexes = {
            @Index(
                    name = "idx_problem_session_user_problem_status_exp",
                    columnList = "user_id,problem_id,status,expires_at")
        })
@Entity
public class ProblemSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProblemSessionStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    private ProblemSession(
            User user, Problem problem, ProblemSessionStatus status, Instant expiresAt) {
        this.user = user;
        this.problem = problem;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public static ProblemSession create(User user, Problem problem, Instant expiresAt) {
        return new ProblemSession(user, problem, ProblemSessionStatus.ACTIVE, expiresAt);
    }

    public boolean isActive() {
        return status == ProblemSessionStatus.ACTIVE;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public void markExpired() {
        if (status == ProblemSessionStatus.ACTIVE) {
            status = ProblemSessionStatus.EXPIRED;
        }
    }

    public void close(Instant closedAt) {
        this.status = ProblemSessionStatus.CLOSED;
        this.closedAt = closedAt;
    }
}
