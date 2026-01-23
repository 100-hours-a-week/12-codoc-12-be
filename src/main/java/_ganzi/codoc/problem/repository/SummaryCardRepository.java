package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.SummaryCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryCardRepository extends JpaRepository<SummaryCard, Long> {
    List<SummaryCard> findByProblemId(Long problemId);
}
