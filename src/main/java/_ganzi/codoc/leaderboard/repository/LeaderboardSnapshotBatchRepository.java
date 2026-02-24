package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshotBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardSnapshotBatchRepository
        extends JpaRepository<LeaderboardSnapshotBatch, Long> {}
