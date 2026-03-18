package _ganzi.codoc.analysis.repository;

import _ganzi.codoc.analysis.domain.job.AnalysisReportJob;
import _ganzi.codoc.analysis.domain.job.AnalysisReportJobStatus;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisReportJobRepository extends JpaRepository<AnalysisReportJob, String> {

    boolean existsByUserIdAndPeriodStartAndPeriodEndAndStatus(
            Long userId, LocalDate periodStart, LocalDate periodEnd, AnalysisReportJobStatus status);

    Optional<AnalysisReportJob> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, AnalysisReportJobStatus status);
}
