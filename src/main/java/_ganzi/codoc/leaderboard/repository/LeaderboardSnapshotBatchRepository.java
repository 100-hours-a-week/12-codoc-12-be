package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshotBatch;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardSnapshotBatchRepository
        extends JpaRepository<LeaderboardSnapshotBatch, Long> {

    Optional<LeaderboardSnapshotBatch> findFirstBySeasonIdOrderByIdDesc(Integer seasonId);
}
