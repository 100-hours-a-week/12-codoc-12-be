package _ganzi.codoc.problem.repository.job;

import _ganzi.codoc.problem.domain.job.RecommendationRequestOutbox;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationRequestOutboxRepository
        extends JpaRepository<RecommendationRequestOutbox, Long> {

    @Query(
            value =
                    """
                    select *
                    from recommendation_request_outbox
                    where next_attempt_at <= :now
                      and (
                            status in ('PENDING', 'FAILED')
                            or (status = 'PROCESSING' and processing_started_at <= :staleThreshold)
                          )
                    order by id asc
                    limit :batchSize
                    for update skip locked
                    """,
            nativeQuery = true)
    List<RecommendationRequestOutbox> lockNextPublishBatch(
            @Param("now") Instant now,
            @Param("staleThreshold") Instant staleThreshold,
            @Param("batchSize") int batchSize);

    @Query(
            """
            select o
            from RecommendationRequestOutbox o
            where o.jobId = :jobId
            """)
    Optional<RecommendationRequestOutbox> findByJobId(@Param("jobId") String jobId);
}
