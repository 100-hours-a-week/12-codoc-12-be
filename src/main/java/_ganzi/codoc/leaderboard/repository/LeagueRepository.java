package _ganzi.codoc.leaderboard.repository;

import _ganzi.codoc.leaderboard.domain.League;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository extends JpaRepository<League, Integer> {

    Optional<League> findByName(String name);
}
