package _ganzi.codoc.submission.repository;

import _ganzi.codoc.submission.domain.UserQuizResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuizResultRepository extends JpaRepository<UserQuizResult, Long> {

    Optional<UserQuizResult> findByAttemptIdAndQuizId(Long attemptId, Long quizId);

    Optional<UserQuizResult> findByAttemptUserIdAndQuizIdAndIdempotencyKey(
            Long userId, Long quizId, String idempotencyKey);

    int countByAttemptId(Long attemptId);

    int countByAttemptIdAndCorrectTrue(Long attemptId);
}
