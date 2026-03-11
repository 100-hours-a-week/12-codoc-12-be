package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardScore;
import _ganzi.codoc.leaderboard.domain.LeaderboardScoreId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaderboardScoreRepository
        extends JpaRepository<LeaderboardScore, LeaderboardScoreId> {

    List<LeaderboardScore> findAllByIdSeasonId(Integer seasonId);

    Optional<LeaderboardScore> findByIdSeasonIdAndIdUserId(Integer seasonId, Long userId);

    long countByIdSeasonId(Integer seasonId);

    long countByIdSeasonIdAndLeagueId(Integer seasonId, Integer leagueId);

    long countByIdSeasonIdAndGroupId(Integer seasonId, Long groupId);

    @Query(
            value =
                    """
                    select *
                    from leaderboard_score s
                    where s.season_id = :seasonId
                    order by s.weekly_xp desc, s.user_id asc
                    limit :limit offset :offset
                    """,
            nativeQuery = true)
    List<LeaderboardScore> findGlobalSlice(
            @Param("seasonId") Integer seasonId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Query(
            value =
                    """
                    select *
                    from leaderboard_score s
                    where s.season_id = :seasonId
                      and s.league_id = :leagueId
                    order by s.weekly_xp desc, s.user_id asc
                    limit :limit offset :offset
                    """,
            nativeQuery = true)
    List<LeaderboardScore> findLeagueSlice(
            @Param("seasonId") Integer seasonId,
            @Param("leagueId") Integer leagueId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Query(
            value =
                    """
                    select *
                    from leaderboard_score s
                    where s.season_id = :seasonId
                      and s.group_id = :groupId
                    order by s.weekly_xp desc, s.user_id asc
                    limit :limit offset :offset
                    """,
            nativeQuery = true)
    List<LeaderboardScore> findGroupSlice(
            @Param("seasonId") Integer seasonId,
            @Param("groupId") Long groupId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Query(
            """
            select count(s) + 1
            from LeaderboardScore s
            where s.id.seasonId = :seasonId
              and (s.weeklyXp > :weeklyXp
                   or (s.weeklyXp = :weeklyXp and s.user.id < :userId))
            """)
    long findGlobalRank(
            @Param("seasonId") Integer seasonId,
            @Param("weeklyXp") int weeklyXp,
            @Param("userId") Long userId);

    @Query(
            """
            select count(s) + 1
            from LeaderboardScore s
            where s.id.seasonId = :seasonId
              and s.league.id = :leagueId
              and (s.weeklyXp > :weeklyXp
                   or (s.weeklyXp = :weeklyXp and s.user.id < :userId))
            """)
    long findLeagueRank(
            @Param("seasonId") Integer seasonId,
            @Param("leagueId") Integer leagueId,
            @Param("weeklyXp") int weeklyXp,
            @Param("userId") Long userId);

    @Query(
            """
            select count(s) + 1
            from LeaderboardScore s
            where s.id.seasonId = :seasonId
              and s.groupId = :groupId
              and (s.weeklyXp > :weeklyXp
                   or (s.weeklyXp = :weeklyXp and s.user.id < :userId))
            """)
    long findGroupRank(
            @Param("seasonId") Integer seasonId,
            @Param("groupId") Long groupId,
            @Param("weeklyXp") int weeklyXp,
            @Param("userId") Long userId);
}
