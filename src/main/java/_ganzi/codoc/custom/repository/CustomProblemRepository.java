package _ganzi.codoc.custom.repository;

import _ganzi.codoc.custom.domain.CustomProblem;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomProblemRepository extends JpaRepository<CustomProblem, Long> {

    @Query(
            """
            select cp
            from CustomProblem cp
            where cp.userId = :userId
              and cp.isDeleted = false
              and (
                :createdAt is null
                or cp.createdAt < :createdAt
                or (cp.createdAt = :createdAt and cp.id < :problemId)
              )
            order by cp.createdAt desc, cp.id desc
            """)
    List<CustomProblem> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("createdAt") Instant createdAt,
            @Param("problemId") Long problemId,
            Pageable pageable);

    @Query(
            """
            select cp
            from CustomProblem cp
            where cp.id = :customProblemId
              and cp.isDeleted = false
            """)
    Optional<CustomProblem> findActiveById(@Param("customProblemId") Long customProblemId);
}
