package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaderboardGroupRepository extends JpaRepository<LeaderboardGroup, Long> {

    @Query(
            """
            select g.id
            from LeaderboardGroup g
            where g.season.seasonId = :seasonId
              and g.league.id = :leagueId
            """)
    List<Long> findIdsBySeasonIdAndLeagueId(
            @Param("seasonId") Integer seasonId, @Param("leagueId") Integer leagueId);
}
