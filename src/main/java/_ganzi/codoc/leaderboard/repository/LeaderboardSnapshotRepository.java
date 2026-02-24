package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, Long> {}
