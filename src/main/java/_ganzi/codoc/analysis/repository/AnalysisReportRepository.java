package _ganzi.codoc.analysis.repository;

import _ganzi.codoc.analysis.domain.AnalysisReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {

    Optional<AnalysisReport> findFirstByUserIdOrderByPeriodEndDesc(Long userId);
}
