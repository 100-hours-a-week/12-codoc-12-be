package _ganzi.codoc.submission.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.submission.enums.QuizAttemptStatus;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "user_quiz_attempt")
@Entity
public class UserQuizAttempt extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_session_id")
    private ProblemSession problemSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private QuizAttemptStatus status;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "abandoned_at")
    private Instant abandonedAt;

    private UserQuizAttempt(User user, Problem problem, QuizAttemptStatus status) {
        this.user = user;
        this.problem = problem;
        this.status = status;
    }

    public static UserQuizAttempt create(User user, Problem problem) {
        return new UserQuizAttempt(user, problem, QuizAttemptStatus.IN_PROGRESS);
    }

    public boolean isInProgress() {
        return this.status == QuizAttemptStatus.IN_PROGRESS;
    }

    public void abandon() {
        if (this.status != QuizAttemptStatus.IN_PROGRESS) {
            return;
        }

        this.status = QuizAttemptStatus.ABANDONED;
        this.abandonedAt = Instant.now();
    }

    public void complete() {
        if (this.status != QuizAttemptStatus.IN_PROGRESS) {
            return;
        }

        this.status = QuizAttemptStatus.COMPLETED;
        this.completedAt = Instant.now();
    }
}
