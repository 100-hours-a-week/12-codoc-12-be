package _ganzi.codoc.submission.repository;

import _ganzi.codoc.problem.enums.QuizType;
import _ganzi.codoc.submission.domain.UserQuizResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserQuizResultRepository extends JpaRepository<UserQuizResult, Long> {

    Optional<UserQuizResult> findByAttemptIdAndQuizId(Long attemptId, Long quizId);

    Optional<UserQuizResult> findByAttemptUserIdAndQuizIdAndIdempotencyKey(
            Long userId, Long quizId, String idempotencyKey);

    List<UserQuizResult> findAllByAttemptId(Long attemptId);

    int countByAttemptId(Long attemptId);

    int countByAttemptIdAndCorrectTrue(Long attemptId);

    @Query(
            """
            select q.quizType as quizType, count(r) as failCount
            from UserQuizResult r
            join r.quiz q
            join r.attempt a
            join a.problemSession ps
            where ps.user.id = :userId
              and r.correct = false
              and r.createdAt between :startAt and :endAt
            group by q.quizType
            """)
    List<QuizFailCount> findQuizFailCounts(
            @Param("userId") Long userId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    interface QuizFailCount {
        QuizType getQuizType();

        long getFailCount();
    }
}
