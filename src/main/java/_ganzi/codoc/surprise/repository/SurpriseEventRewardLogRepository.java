package _ganzi.codoc.surprise.repository;

import _ganzi.codoc.surprise.domain.SurpriseEventRewardLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurpriseEventRewardLogRepository
        extends JpaRepository<SurpriseEventRewardLog, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
