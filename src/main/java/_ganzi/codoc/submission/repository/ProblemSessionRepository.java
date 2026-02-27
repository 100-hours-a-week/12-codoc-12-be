package _ganzi.codoc.submission.repository;

import _ganzi.codoc.submission.domain.ProblemSession;
import _ganzi.codoc.submission.enums.ProblemSessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemSessionRepository extends JpaRepository<ProblemSession, Long> {

    Optional<ProblemSession> findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
            Long userId, Long problemId, ProblemSessionStatus status);

    Optional<ProblemSession> findFirstByUserIdAndStatusOrderByIdDesc(
            Long userId, ProblemSessionStatus status);

    @Query(
            """
            select ps
            from ProblemSession ps
            where ps.user.id = :userId
              and ps.createdAt <= :endAt
              and coalesce(ps.closedAt, ps.expiresAt) >= :startAt
            """)
    List<ProblemSession> findOverlappingSessions(
            @Param("userId") Long userId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);
}
