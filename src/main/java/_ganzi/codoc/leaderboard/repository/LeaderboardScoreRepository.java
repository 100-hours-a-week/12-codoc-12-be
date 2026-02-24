package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardScore;
import _ganzi.codoc.leaderboard.domain.LeaderboardScoreId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardScoreRepository
        extends JpaRepository<LeaderboardScore, LeaderboardScoreId> {

    List<LeaderboardScore> findAllByIdSeasonId(Integer seasonId);
}
