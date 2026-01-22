package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.enums.ProblemLevel;
import _ganzi.codoc.problem.enums.ProblemSolvingStatus;
import _ganzi.codoc.problem.dto.ProblemListItem;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query(
            """
            select new _ganzi.codoc.problem.dto.ProblemListItem(
                p.id,
                p.title,
                p.level,
                coalesce(upr.status, :defaultStatus),
                (b.id is not null)
            )
            from Problem p
            left join UserProblemResult upr on upr.problem = p and upr.user.id = :userId
            left join Bookmark b on b.problem = p and b.user.id = :userId
            where (:cursor is null or p.id > :cursor)
              and p.level in :levels
              and (
                    upr.status in :statuses
                    or (upr is null and :defaultStatus in :statuses)
                  )
              and (:bookmarked = false or b.id is not null)
            order by p.id asc
            """)
    List<ProblemListItem> findProblemList(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            @Param("levels") List<ProblemLevel> levels,
            @Param("statuses") List<ProblemSolvingStatus> statuses,
            @Param("defaultStatus") ProblemSolvingStatus defaultStatus,
            @Param("bookmarked") boolean bookmarked,
            Pageable pageable);

    @Query(
            """
            select new _ganzi.codoc.problem.dto.ProblemListItem(
                p.id,
                p.title,
                p.level,
                coalesce(upr.status, :defaultStatus),
                (b.id is not null)
            )
            from Problem p
            left join UserProblemResult upr on upr.problem = p and upr.user.id = :userId
            left join Bookmark b on b.problem = p and b.user.id = :userId
            where (:cursor is null or p.id > :cursor)
              and p.level in :levels
              and (
                    upr.status in :statuses
                    or (upr is null and :defaultStatus in :statuses)
                  )
              and (:bookmarked = false or b.id is not null)
              and lower(p.title) like lower(concat('%', :query, '%'))
            order by p.id asc
            """)
    List<ProblemListItem> searchProblemList(
            @Param("userId") Long userId,
            @Param("query") String query,
            @Param("cursor") Long cursor,
            @Param("levels") List<ProblemLevel> levels,
            @Param("statuses") List<ProblemSolvingStatus> statuses,
            @Param("defaultStatus") ProblemSolvingStatus defaultStatus,
            @Param("bookmarked") boolean bookmarked,
            Pageable pageable);
}
