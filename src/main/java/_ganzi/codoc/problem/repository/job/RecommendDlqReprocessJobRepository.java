package _ganzi.codoc.problem.repository.job;

import _ganzi.codoc.problem.domain.job.RecommendDlqReprocessJob;
import _ganzi.codoc.problem.domain.job.RecommendDlqReprocessJobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendDlqReprocessJobRepository
        extends JpaRepository<RecommendDlqReprocessJob, Long> {

    @Query(
            """
            select j
            from RecommendDlqReprocessJob j
            where j.status in :statuses
            order by j.id desc
            """)
    List<RecommendDlqReprocessJob> findAllByStatuses(
            @Param("statuses") List<RecommendDlqReprocessJobStatus> statuses);

    default Optional<RecommendDlqReprocessJob> findLatestRunningJob() {
        List<RecommendDlqReprocessJob> jobs =
                findAllByStatuses(
                        List.of(
                                RecommendDlqReprocessJobStatus.QUEUED,
                                RecommendDlqReprocessJobStatus.RUNNING));
        return jobs.isEmpty() ? Optional.empty() : Optional.of(jobs.get(0));
    }
}
