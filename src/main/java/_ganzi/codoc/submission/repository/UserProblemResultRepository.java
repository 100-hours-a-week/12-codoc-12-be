package _ganzi.codoc.submission.repository;

import _ganzi.codoc.submission.domain.UserProblemResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProblemResultRepository extends JpaRepository<UserProblemResult, Long> {
    Optional<UserProblemResult> findByUserIdAndProblemId(Long userId, Long problemId);
}
