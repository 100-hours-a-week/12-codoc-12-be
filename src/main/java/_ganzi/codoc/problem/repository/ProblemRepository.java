package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.dto.ProblemListItem;
import _ganzi.codoc.problem.dto.ProblemSearchParam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query(
            """
            select new _ganzi.codoc.problem.dto.ProblemListItem(
                p.id,
                p.title,
                p.difficulty,
                coalesce(upr.status, :#{#params.defaultStatus}),
                (b.id is not null)
            )
            from Problem p
            left join UserProblemResult upr on upr.problem = p and upr.user.id = :#{#params.userId}
            left join Bookmark b on b.problem = p and b.user.id = :#{#params.userId}
            where (p.id > :#{#params.cursor})
              and p.difficulty in :#{#params.difficulties}
              and (
                    upr.status in :#{#params.statuses}
                    or (upr is null and :#{#params.defaultStatus} in :#{#params.statuses})
                  )
              and (:#{#params.bookmarked} = false or b.id is not null)
            order by p.id asc
            """)
    List<ProblemListItem> findProblemList(@Param("params") ProblemSearchParam params);

    @Query(
            """
            select new _ganzi.codoc.problem.dto.ProblemListItem(
                p.id,
                p.title,
                p.difficulty,
                coalesce(upr.status, :#{#params.defaultStatus}),
                (b.id is not null)
            )
            from Problem p
            left join UserProblemResult upr on upr.problem = p and upr.user.id = :#{#params.userId}
            left join Bookmark b on b.problem = p and b.user.id = :#{#params.userId}
            where (p.id > :#{#params.cursor})
              and p.difficulty in :#{#params.difficulties}
              and (
                    upr.status in :#{#params.statuses}
                    or (upr is null and :#{#params.defaultStatus} in :#{#params.statuses})
                  )
              and (:#{#params.bookmarked} = false or b.id is not null)
              and lower(p.title) like lower(concat('%', :#{#params.query}, '%'))
            order by p.id asc
            """)
    List<ProblemListItem> searchProblemList(@Param("params") ProblemSearchParam params);
}
