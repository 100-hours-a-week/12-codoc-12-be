package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardScopeType;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, Long> {

    @Query(
            """
            select s
            from LeaderboardSnapshot s
            join fetch s.user u
            join fetch u.avatar
            where s.snapshotBatch.id = :snapshotId
              and s.scopeType = :scopeType
              and s.scopeId is null
              and s.rank between :startRank and :endRank
            order by s.rank
            """)
    List<LeaderboardSnapshot> findGlobalSnapshots(
            @Param("snapshotId") Long snapshotId,
            @Param("scopeType") LeaderboardScopeType scopeType,
            @Param("startRank") int startRank,
            @Param("endRank") int endRank);

    @Query(
            """
            select s
            from LeaderboardSnapshot s
            join fetch s.user u
            join fetch u.avatar
            where s.snapshotBatch.id = :snapshotId
              and s.scopeType = :scopeType
              and s.scopeId = :scopeId
              and s.rank between :startRank and :endRank
            order by s.rank
            """)
    List<LeaderboardSnapshot> findScopedSnapshots(
            @Param("snapshotId") Long snapshotId,
            @Param("scopeType") LeaderboardScopeType scopeType,
            @Param("scopeId") Long scopeId,
            @Param("startRank") int startRank,
            @Param("endRank") int endRank);

    @Query(
            """
            select s
            from LeaderboardSnapshot s
            join fetch s.user u
            join fetch u.avatar
            where s.snapshotBatch.id = :snapshotId
              and s.scopeType = :scopeType
              and s.scopeId is null
              and u.id = :userId
            """)
    Optional<LeaderboardSnapshot> findGlobalSnapshotByUser(
            @Param("snapshotId") Long snapshotId,
            @Param("scopeType") LeaderboardScopeType scopeType,
            @Param("userId") Long userId);

    @Query(
            """
            select s
            from LeaderboardSnapshot s
            join fetch s.user u
            join fetch u.avatar
            where s.snapshotBatch.id = :snapshotId
              and s.scopeType = :scopeType
              and s.scopeId = :scopeId
              and u.id = :userId
            """)
    Optional<LeaderboardSnapshot> findScopedSnapshotByUser(
            @Param("snapshotId") Long snapshotId,
            @Param("scopeType") LeaderboardScopeType scopeType,
            @Param("scopeId") Long scopeId,
            @Param("userId") Long userId);

    @Query(
            """
            select s.scopeId
            from LeaderboardSnapshot s
            where s.snapshotBatch.id = :snapshotId
              and s.scopeType = :scopeType
              and s.user.id = :userId
            """)
    Optional<Long> findScopeIdBySnapshotAndUser(
            @Param("snapshotId") Long snapshotId,
            @Param("scopeType") LeaderboardScopeType scopeType,
            @Param("userId") Long userId);

    List<LeaderboardSnapshot> findBySnapshotBatchIdAndScopeTypeAndScopeIdAndWeeklyXpGreaterThanOrderByRankAsc(
            Long snapshotId, LeaderboardScopeType scopeType, Long scopeId, int weeklyXp, Pageable pageable);

    List<LeaderboardSnapshot> findBySnapshotBatchIdAndScopeTypeAndScopeIdOrderByRankDesc(
            Long snapshotId, LeaderboardScopeType scopeType, Long scopeId, Pageable pageable);

}
