package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.LeaderboardPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardPolicyRepository extends JpaRepository<LeaderboardPolicy, Integer> {

    Optional<LeaderboardPolicy> findByLeagueId(Integer leagueId);
}
