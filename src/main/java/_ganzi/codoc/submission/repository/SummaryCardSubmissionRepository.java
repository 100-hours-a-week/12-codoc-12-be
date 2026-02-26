package _ganzi.codoc.submission.repository;

import _ganzi.codoc.submission.domain.SummaryCardSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryCardSubmissionRepository
        extends JpaRepository<SummaryCardSubmission, Long> {}
