package _ganzi.codoc.surprise.repository;

import _ganzi.codoc.surprise.domain.SurpriseQuizSubmission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurpriseQuizSubmissionRepository
        extends JpaRepository<SurpriseQuizSubmission, Long> {

    Optional<SurpriseQuizSubmission> findByEventIdAndUserId(Long eventId, Long userId);

    long countByEventIdAndCorrectTrue(Long eventId);

    List<SurpriseQuizSubmission>
            findAllByEventIdAndCorrectTrueOrderByElapsedMillisAscSubmittedAtAscUserIdAsc(Long eventId);

    List<SurpriseQuizSubmission> findAllByEventId(Long eventId);
}
