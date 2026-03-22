package _ganzi.codoc.surprise.repository;

import _ganzi.codoc.surprise.domain.SurpriseEvent;
import _ganzi.codoc.surprise.domain.SurpriseEventStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurpriseEventRepository extends JpaRepository<SurpriseEvent, Long> {

    @Query("select e from SurpriseEvent e join fetch e.quizPool where e.id = :eventId")
    Optional<SurpriseEvent> findByIdWithQuizPool(@Param("eventId") Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from SurpriseEvent e join fetch e.quizPool where e.id = :eventId")
    Optional<SurpriseEvent> findByIdForUpdate(@Param("eventId") Long eventId);

    boolean existsByEventWeekKey(String eventWeekKey);

    List<SurpriseEvent> findAllByStatusAndStartsAtLessThanEqual(
            SurpriseEventStatus status, Instant startsAt);

    List<SurpriseEvent> findAllByStatusAndEndsAtLessThanEqualAndSettledAtIsNull(
            SurpriseEventStatus status, Instant endsAt);
}
