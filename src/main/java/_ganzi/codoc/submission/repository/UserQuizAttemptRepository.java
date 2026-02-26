package _ganzi.codoc.submission.repository;

import _ganzi.codoc.submission.domain.UserQuizAttempt;
import _ganzi.codoc.submission.enums.QuizAttemptStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {

    Optional<UserQuizAttempt> findByIdAndUserIdAndProblemId(Long id, Long userId, Long problemId);

    List<UserQuizAttempt> findAllByUserIdAndProblemIdAndStatus(
            Long userId, Long problemId, QuizAttemptStatus status);

    Optional<UserQuizAttempt> findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
            Long userId, Long problemId, QuizAttemptStatus status);

    Optional<UserQuizAttempt> findFirstByProblemSessionIdAndStatusOrderByIdDesc(
            Long problemSessionId, QuizAttemptStatus status);
}
