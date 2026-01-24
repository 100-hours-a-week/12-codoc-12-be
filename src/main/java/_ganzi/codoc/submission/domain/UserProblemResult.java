package _ganzi.codoc.submission.domain;

import _ganzi.codoc.global.domain.BaseTimeEntity;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.exception.InvalidProblemResultEvaluationException;
import _ganzi.codoc.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_problem_result",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "problem_id"})})
@Entity
public class UserProblemResult extends BaseTimeEntity {

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
    @Column(name = "status", nullable = false, length = 50)
    private ProblemSolvingStatus status;

    private UserProblemResult(User user, Problem problem, ProblemSolvingStatus status) {
        this.user = user;
        this.problem = problem;
        this.status = status;
    }

    public static UserProblemResult createForSummaryCard(
            User user, Problem problem, boolean allCorrect) {
        ProblemSolvingStatus status =
                ProblemSolvingStatus.NOT_ATTEMPTED.nextStatusForSummaryCard(allCorrect);
        return new UserProblemResult(user, problem, status);
    }

    public void applyNextStatusForSummaryCard(boolean allCorrect) {
        this.status = this.status.nextStatusForSummaryCard(allCorrect);
    }

    public boolean isSolved() {
        return this.status == ProblemSolvingStatus.SOLVED;
    }

    public void markSolved() {
        this.status = ProblemSolvingStatus.SOLVED;
    }

    public void validateCanEvaluateProblemResult() {
        if (this.status != ProblemSolvingStatus.SUMMARY_PASSED
                && this.status != ProblemSolvingStatus.SOLVED) {
            throw new InvalidProblemResultEvaluationException();
        }
    }
}
