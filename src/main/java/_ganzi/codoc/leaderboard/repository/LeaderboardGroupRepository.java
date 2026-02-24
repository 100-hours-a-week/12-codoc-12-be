package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardGroupRepository extends JpaRepository<LeaderboardGroup, Long> {}
