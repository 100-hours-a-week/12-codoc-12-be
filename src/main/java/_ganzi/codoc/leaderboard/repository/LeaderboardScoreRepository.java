package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardScore;
import _ganzi.codoc.leaderboard.domain.LeaderboardScoreId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardScoreRepository
        extends JpaRepository<LeaderboardScore, LeaderboardScoreId> {}
