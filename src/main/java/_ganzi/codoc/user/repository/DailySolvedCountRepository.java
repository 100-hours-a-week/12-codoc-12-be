package _ganzi.codoc.user.repository;

import _ganzi.codoc.user.domain.DailySolvedCount;
import _ganzi.codoc.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySolvedCountRepository extends JpaRepository<DailySolvedCount, Long> {

    Optional<DailySolvedCount> findByUserAndDate(User user, LocalDate date);

    List<DailySolvedCount> findAllByUserAndDateBetweenAndSolvedCountGreaterThanOrderByDateAsc(
            User user, LocalDate fromDate, LocalDate toDate, int minCount);
}
