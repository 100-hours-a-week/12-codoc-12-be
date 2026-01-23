package _ganzi.codoc.user.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySolvedCountRepository extends JpaRepository<DailySolvedCount, Long> {

    Optional<DailySolvedCount> findByUserIdAndDate(User user, LocalDate date);

    List<DailySolvedCount> findAllByUserIdAndDateBetweenOrderByDateAsc(
            User user, LocalDate fromDate, LocalDate toDate);
}
