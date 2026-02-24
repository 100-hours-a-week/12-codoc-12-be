package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardSeasonRepository extends JpaRepository<LeaderboardSeason, Integer> {

    Optional<LeaderboardSeason> findFirstByStartsAtLessThanEqualAndEndsAtAfterOrderByStartsAtDesc(
            Instant now, Instant now2);
}
