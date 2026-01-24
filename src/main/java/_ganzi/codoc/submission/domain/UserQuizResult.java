package _ganzi.codoc.submission.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.domain.Quiz;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_quiz_result",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"attempt_id", "quiz_id"})})
@Entity
public class UserQuizResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private UserQuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    private UserQuizResult(
            UserQuizAttempt attempt, Quiz quiz, String idempotencyKey, boolean correct) {
        this.attempt = attempt;
        this.quiz = quiz;
        this.idempotencyKey = idempotencyKey;
        this.correct = correct;
    }

    public static UserQuizResult create(
            UserQuizAttempt attempt, Quiz quiz, String idempotencyKey, boolean correct) {
        return new UserQuizResult(attempt, quiz, idempotencyKey, correct);
    }

    public boolean isIdempotencyKeySame(String key) {
        return this.idempotencyKey.equals(key);
    }
}
