package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.SummaryCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface SummaryCardRepository extends JpaRepository<SummaryCard, Long> {

    List<SummaryCard> findByProblemIdOrderByParagraphOrderAsc(@Param("problemId") Long problemId);
}
