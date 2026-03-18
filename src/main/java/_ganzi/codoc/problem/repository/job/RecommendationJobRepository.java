package _ganzi.codoc.problem.repository.job;

import _ganzi.codoc.problem.domain.job.RecommendationJob;
import _ganzi.codoc.problem.domain.job.RecommendationJobStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationJobRepository extends JpaRepository<RecommendationJob, String> {

    boolean existsByUserIdAndStatus(Long userId, RecommendationJobStatus status);

    Optional<RecommendationJob> findByJobIdAndUserId(String jobId, Long userId);

    Optional<RecommendationJob> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, RecommendationJobStatus status);

    Optional<RecommendationJob> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
