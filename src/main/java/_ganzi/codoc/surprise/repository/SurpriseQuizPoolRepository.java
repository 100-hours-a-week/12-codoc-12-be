package _ganzi.codoc.surprise.repository;

import _ganzi.codoc.surprise.domain.SurpriseQuizPool;
import _ganzi.codoc.surprise.domain.SurpriseQuizPoolStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SurpriseQuizPoolRepository extends JpaRepository<SurpriseQuizPool, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SurpriseQuizPool> findFirstByStatusOrderByIdAsc(SurpriseQuizPoolStatus status);
}
