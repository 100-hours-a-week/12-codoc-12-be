package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardSeasonRepository extends JpaRepository<LeaderboardSeason, Integer> {}
