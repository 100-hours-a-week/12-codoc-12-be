package _ganzi.codoc.custom.repository;

import _ganzi.codoc.custom.domain.CustomSummaryCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomSummaryCardRepository extends JpaRepository<CustomSummaryCard, Long> {

    @Query(
            """
            select sc
            from CustomSummaryCard sc
            where sc.customProblem.id = :customProblemId
            order by sc.paragraphOrder asc
            """)
    List<CustomSummaryCard> findAllByCustomProblemId(
            @Param("customProblemId") Long customProblemId);
}
