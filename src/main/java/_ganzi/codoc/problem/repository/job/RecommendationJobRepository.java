package _ganzi.codoc.problem.repository.job;

import _ganzi.codoc.problem.domain.job.RecommendationJob;
import _ganzi.codoc.problem.domain.job.RecommendationJobStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationJobRepository extends JpaRepository<RecommendationJob, String> {

    @Query(
            """
            select case when count(j) > 0 then true else false end
            from RecommendationJob j
            where j.userId = :userId
              and j.status in :statuses
            """)
    boolean existsByUserIdAndStatuses(
            @Param("userId") Long userId,
            @Param("statuses") Collection<RecommendationJobStatus> statuses);

    @Query(
            """
            select j
            from RecommendationJob j
            where j.jobId = :jobId
              and j.userId = :userId
            """)
    Optional<RecommendationJob> findByJobIdAndUserId(
            @Param("jobId") String jobId, @Param("userId") Long userId);

    @Query(
            """
            select j
            from RecommendationJob j
            where j.userId = :userId
              and j.status in :statuses
            order by j.createdAt desc
            """)
    List<RecommendationJob> findAllByUserIdAndStatusesOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("statuses") Collection<RecommendationJobStatus> statuses);

    @Query(
            """
            select j
            from RecommendationJob j
            where j.userId = :userId
            order by j.createdAt desc
            """)
    List<RecommendationJob> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    default Optional<RecommendationJob> findLatestByUserIdAndStatuses(
            Long userId, Collection<RecommendationJobStatus> statuses) {
        return findAllByUserIdAndStatusesOrderByCreatedAtDesc(userId, statuses).stream().findFirst();
    }

    default Optional<RecommendationJob> findLatestByUserId(Long userId) {
        return findAllByUserIdOrderByCreatedAtDesc(userId).stream().findFirst();
    }
}
