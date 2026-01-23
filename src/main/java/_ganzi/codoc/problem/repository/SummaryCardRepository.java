package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.SummaryCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SummaryCardRepository extends JpaRepository<SummaryCard, Long> {

    @Query(
            """
            select summaryCard
            from SummaryCard summaryCard
            join fetch summaryCard.summaryCardTag summaryCardTag
            where summaryCard.problem.id = :problemId
            order by summaryCardTag.sequence asc
            """)
    List<SummaryCard> findByProblemIdOrderBySummaryCardTagSequenceAsc(@Param("problemId") Long problemId);
}
