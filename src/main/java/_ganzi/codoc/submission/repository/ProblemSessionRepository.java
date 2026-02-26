package _ganzi.codoc.submission.repository;

import _ganzi.codoc.submission.domain.ProblemSession;
import _ganzi.codoc.submission.enums.ProblemSessionStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemSessionRepository extends JpaRepository<ProblemSession, Long> {

    Optional<ProblemSession> findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
            Long userId, Long problemId, ProblemSessionStatus status);
}
