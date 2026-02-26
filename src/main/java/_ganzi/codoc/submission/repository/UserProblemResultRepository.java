package _ganzi.codoc.submission.repository;

import _ganzi.codoc.submission.domain.UserProblemResult;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProblemResultRepository extends JpaRepository<UserProblemResult, Long> {
    Optional<UserProblemResult> findByUserIdAndProblemId(Long userId, Long problemId);

    List<UserProblemResult> findAllByUserId(Long userId);

    long countByUserIdAndStatusAndUpdatedAtBetween(
            Long userId, ProblemSolvingStatus status, Instant startAt, Instant endAt);
}
