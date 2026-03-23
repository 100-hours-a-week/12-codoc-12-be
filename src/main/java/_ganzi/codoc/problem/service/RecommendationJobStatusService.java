package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.domain.job.RecommendationJob;
import _ganzi.codoc.problem.repository.job.RecommendationJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecommendationJobStatusService {

    private final RecommendationJobRepository recommendationJobRepository;

    @Transactional
    public void markPublished(String jobId) {
        recommendationJobRepository.findById(jobId).ifPresent(RecommendationJob::markPublished);
    }

    @Transactional
    public void markPublishFailed(String jobId, String errorMessage) {
        recommendationJobRepository
                .findById(jobId)
                .ifPresent(job -> job.markPublishFailed("PUBLISH_FAILED", errorMessage));
    }
}
