package _ganzi.codoc.problem.repository;

import _ganzi.codoc.problem.domain.RecommendedProblem;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendedProblemRepository extends JpaRepository<RecommendedProblem, Long> {

    long countByUserIdAndIsDoneFalse(Long userId);

    @Query(
            "select rp.problem.id from RecommendedProblem rp "
                    + "where rp.user.id = :userId and rp.isDone = false")
    List<Long> findPendingProblemIds(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RecommendedProblem rp set rp.isDone = true where rp.user.id = :userId")
    int markAllDoneByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "update RecommendedProblem rp "
                    + "set rp.isDone = true, rp.solvedAt = :solvedAt "
                    + "where rp.user.id = :userId and rp.problem.id = :problemId and rp.isDone = false")
    int markDoneByUserIdAndProblemId(
            @Param("userId") Long userId,
            @Param("problemId") Long problemId,
            @Param("solvedAt") Instant solvedAt);
}
