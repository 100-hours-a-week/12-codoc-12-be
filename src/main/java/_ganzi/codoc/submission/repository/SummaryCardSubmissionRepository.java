package _ganzi.codoc.submission.repository;

import _ganzi.codoc.problem.enums.ParagraphType;
import _ganzi.codoc.submission.domain.SummaryCardSubmission;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SummaryCardSubmissionRepository
        extends JpaRepository<SummaryCardSubmission, Long> {

    @Query(
            """
            select sc.paragraphType as paragraphType, count(s) as failCount
            from SummaryCardSubmission s
            join s.summaryCard sc
            join s.attempt a
            join a.problemSession ps
            where ps.user.id = :userId
              and s.correct = false
              and s.submittedAt between :startAt and :endAt
            group by sc.paragraphType
            """)
    List<ParagraphFailCount> findParagraphFailCounts(
            @Param("userId") Long userId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    interface ParagraphFailCount {
        ParagraphType getParagraphType();

        long getFailCount();
    }
}
