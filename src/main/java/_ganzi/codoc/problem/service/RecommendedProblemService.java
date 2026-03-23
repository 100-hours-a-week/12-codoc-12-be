package _ganzi.codoc.problem.service;

import _ganzi.codoc.global.exception.ResourceNotFoundException;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.service.NotificationDispatchService;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.RecommendedProblem;
import _ganzi.codoc.problem.domain.job.RecommendationJob;
import _ganzi.codoc.problem.domain.job.RecommendationJobStatus;
import _ganzi.codoc.problem.event.RecommendationOutboxPublishRequestedEvent;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.RecommendedProblemRepository;
import _ganzi.codoc.problem.repository.job.RecommendationJobRepository;
import _ganzi.codoc.problem.service.recommend.RecommendClient;
import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import _ganzi.codoc.problem.service.recommend.dto.RecommendResponse;
import _ganzi.codoc.submission.domain.UserProblemResult;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.repository.UserProblemResultRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.enums.InitLevel;
import _ganzi.codoc.user.enums.UserStatus;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendedProblemService {

    private static final int REPLENISH_THRESHOLD = 1;
    private static final int LOCK_TIMEOUT_SECONDS = 2;
    private static final long RATE_LIMIT_BACKOFF_MILLIS = 500;
    private static final String RECOMMEND_ISSUED_TITLE = "AI 추천 문제가 발급됐어요";
    private static final String RECOMMEND_ISSUED_BODY = "새 추천 문제를 확인해보세요.";

    private final RecommendedProblemRepository recommendedProblemRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final UserProblemResultRepository userProblemResultRepository;
    private final EntityManager entityManager;
    private final RecommendClient recommendClient;
    private final NotificationDispatchService notificationDispatchService;
    private final RecommendationJobRepository recommendationJobRepository;
    private final RecommendationRequestOutboxService recommendationRequestOutboxService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectProvider<RecommendedProblemService> recommendedProblemServiceProvider;

    @Qualifier("recommendOnDemandTaskExecutor")
    private final TaskExecutor recommendOnDemandTaskExecutor;

    @Value("${app.recommend.mq.enabled:false}")
    private boolean recommendMqEnabled;

    @Value("${app.recommend.mq.request-publish-enabled:false}")
    private boolean recommendMqRequestPublishEnabled;

    @Transactional
    public void issueDailyRecommendations() {
        List<Long> userIds =
                userRepository.findAllByStatus(UserStatus.ACTIVE).stream().map(User::getId).toList();
        for (Long userId : userIds) {
            requestRecommendationJobAsync(userId, RecommendationScenario.DAILY, true);
        }
    }

    @Transactional
    public void replenishIfNeeded(Long userId) {
        long pendingCount = recommendedProblemRepository.countByUserIdAndIsDoneFalse(userId);
        if (pendingCount > REPLENISH_THRESHOLD) {
            return;
        }
        requestRecommendationJobAsync(userId, RecommendationScenario.ON_DEMAND, false);
    }

    @Transactional
    public void issueRecommendationsForUser(Long userId, RecommendationScenario scenario) {
        if (!acquireUserLock(userId)) {
            return;
        }
        try {
            if (scenario == RecommendationScenario.ON_DEMAND
                    && recommendedProblemRepository.countByUserIdAndIsDoneFalse(userId)
                            > REPLENISH_THRESHOLD) {
                return;
            }
            RecommendRequest request = buildRecommendRequest(userId, scenario);
            RecommendResponse response = recommendClient.requestRecommendations(request);
            if (response.recommendations().isEmpty()) {
                return;
            }
            applyRecommendations(userId, scenario, responseToCandidates(response.recommendations()));
        } finally {
            releaseUserLock(userId);
        }
    }

    @Transactional
    public RecommendationJob requestOnDemandJob(Long userId) {
        return requestRecommendationJobAsync(userId, RecommendationScenario.ON_DEMAND, true)
                .orElseThrow(() -> new IllegalStateException("Failed to create recommendation job"));
    }

    public RecommendationJob getRecommendationJob(Long userId, String jobId) {
        return recommendationJobRepository
                .findByJobIdAndUserId(jobId, userId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public void applyRecommendationsFromResponse(
            Long userId, RecommendationScenario scenario, RecommendResponse response) {
        if (!acquireUserLock(userId)) {
            return;
        }
        try {
            if (response.recommendations().isEmpty()) {
                return;
            }
            applyRecommendations(userId, scenario, responseToCandidates(response.recommendations()));
        } finally {
            releaseUserLock(userId);
        }
    }

    @Transactional
    public void markProblemSolved(Long userId, Long problemId) {
        recommendedProblemRepository.markDoneByUserIdAndProblemId(userId, problemId, Instant.now());
        try {
            replenishIfNeeded(userId);
        } catch (Exception exception) {
            log.warn(
                    "Recommend replenish failed after solve. userId={}, problemId={}",
                    userId,
                    problemId,
                    exception);
        }
    }

    public RecommendRequest buildRecommendRequest(Long userId, RecommendationScenario scenario) {
        RecommendationFilterInfo filterInfo = buildRecommendationFilterInfo(userId, scenario);
        InitLevel userLevel =
                userRepository
                        .findById(userId)
                        .map(User::getInitLevel)
                        .orElseThrow(UserNotFoundException::new);
        RecommendationScenario requestScenario = scenario;
        if (filterInfo.solvedProblemIds().size() < 5) {
            requestScenario = RecommendationScenario.NEW;
        }
        return RecommendRequest.of(userId, userLevel, requestScenario, filterInfo);
    }

    private RecommendationFilterInfo buildRecommendationFilterInfo(
            Long userId, RecommendationScenario scenario) {
        List<UserProblemResult> results = userProblemResultRepository.findAllByUserId(userId);
        List<Long> solvedIds = new ArrayList<>();
        List<Long> challengeIds = new ArrayList<>();
        for (UserProblemResult result : results) {
            Long problemId = result.getProblem().getId();
            if (result.getStatus() == ProblemSolvingStatus.SOLVED) {
                solvedIds.add(problemId);
            } else {
                challengeIds.add(problemId);
            }
        }
        if (scenario == RecommendationScenario.ON_DEMAND) {
            challengeIds.addAll(recommendedProblemRepository.findPendingProblemIds(userId));
        }
        return new RecommendationFilterInfo(solvedIds, challengeIds);
    }

    private Optional<RecommendationJob> publishRecommendationJob(
            Long userId, RecommendationScenario scenario, boolean forcePublish) {
        if (!recommendMqEnabled || !recommendMqRequestPublishEnabled) {
            return Optional.empty();
        }
        if (!forcePublish
                && scenario == RecommendationScenario.ON_DEMAND
                && recommendedProblemRepository.countByUserIdAndIsDoneFalse(userId) > REPLENISH_THRESHOLD) {
            return Optional.empty();
        }
        List<RecommendationJobStatus> activeStatuses =
                List.of(RecommendationJobStatus.REQUESTED, RecommendationJobStatus.PUBLISHED);
        if (recommendationJobRepository.existsByUserIdAndStatuses(userId, activeStatuses)) {
            return recommendationJobRepository.findLatestByUserIdAndStatuses(userId, activeStatuses);
        }

        String jobId = UUID.randomUUID().toString();
        Instant requestedAt = Instant.now();
        RecommendationJob job = RecommendationJob.publish(jobId, userId, scenario, requestedAt);
        recommendationJobRepository.save(job);

        try {
            RecommendRequest request = buildRecommendRequest(userId, scenario);
            recommendationRequestOutboxService.enqueue(jobId, requestedAt, request);
            applicationEventPublisher.publishEvent(
                    new RecommendationOutboxPublishRequestedEvent(jobId, request, requestedAt));
            return Optional.of(job);
        } catch (Exception exception) {
            job.markPublishFailed("PUBLISH_FAILED", exception.getMessage());
            log.warn("recommend publish failed. jobId={}, userId={}", jobId, userId, exception);
            return Optional.of(job);
        }
    }

    private Optional<RecommendationJob> requestRecommendationJobAsync(
            Long userId, RecommendationScenario scenario, boolean forcePublish) {
        if (recommendMqEnabled && recommendMqRequestPublishEnabled) {
            return publishRecommendationJob(userId, scenario, forcePublish);
        }

        if (!forcePublish
                && scenario == RecommendationScenario.ON_DEMAND
                && recommendedProblemRepository.countByUserIdAndIsDoneFalse(userId) > REPLENISH_THRESHOLD) {
            return Optional.empty();
        }

        List<RecommendationJobStatus> activeStatuses =
                List.of(RecommendationJobStatus.REQUESTED, RecommendationJobStatus.PUBLISHED);
        Optional<RecommendationJob> existing =
                recommendationJobRepository.findLatestByUserIdAndStatuses(userId, activeStatuses);
        if (existing.isPresent()) {
            return existing;
        }

        String jobId = UUID.randomUUID().toString();
        Instant requestedAt = Instant.now();
        RecommendationJob job =
                recommendationJobRepository.save(
                        RecommendationJob.publish(jobId, userId, scenario, requestedAt));
        recommendOnDemandTaskExecutor.execute(() -> processFallbackJob(jobId, userId, scenario));
        return Optional.of(job);
    }

    private void applyRecommendations(
            Long userId, RecommendationScenario scenario, List<RecommendationCandidate> candidates) {
        if (candidates.isEmpty()) {
            return;
        }
        if (scenario == RecommendationScenario.ON_DEMAND
                && recommendedProblemRepository.countByUserIdAndIsDoneFalse(userId) > REPLENISH_THRESHOLD) {
            return;
        }

        if (scenario == RecommendationScenario.DAILY) {
            recommendedProblemRepository.markAllDoneByUserId(userId);
        }

        RecommendationFilterInfo filterInfo = buildRecommendationFilterInfo(userId, scenario);
        Set<Long> existingPendingIds =
                scenario == RecommendationScenario.ON_DEMAND
                        ? new HashSet<>(recommendedProblemRepository.findPendingProblemIds(userId))
                        : Set.of();

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Set<Long> solvedProblemIds = new HashSet<>(filterInfo.solvedProblemIds());
        List<RecommendedProblem> toSave = new ArrayList<>();
        for (RecommendationCandidate candidate : candidates) {
            if (solvedProblemIds.contains(candidate.problemId())) {
                continue;
            }
            if (existingPendingIds.contains(candidate.problemId())) {
                continue;
            }
            Problem problem = problemRepository.findById(candidate.problemId()).orElse(null);
            if (problem == null) {
                continue;
            }
            toSave.add(RecommendedProblem.create(user, problem, candidate.reasonMsg()));
        }
        if (!toSave.isEmpty()) {
            recommendedProblemRepository.saveAll(toSave);
            notificationDispatchService.dispatchAfterCommit(
                    userId,
                    new NotificationMessageItem(
                            NotificationType.AI_RECOMMENDED_PROBLEM_ISSUED,
                            RECOMMEND_ISSUED_TITLE,
                            RECOMMEND_ISSUED_BODY,
                            java.util.Map.of(
                                    "scenario", scenario.name(),
                                    "count", String.valueOf(toSave.size()))));
        }
    }

    private List<RecommendationCandidate> responseToCandidates(
            List<RecommendResponse.RecommendationItem> responseItems) {
        return responseItems.stream()
                .map(item -> new RecommendationCandidate(item.problemId(), item.reasonMsg()))
                .toList();
    }

    private boolean acquireUserLock(Long userId) {
        String lockName = lockName(userId);
        Number acquired =
                (Number)
                        entityManager
                                .createNativeQuery("SELECT GET_LOCK(:lockName, :timeoutSeconds)")
                                .setParameter("lockName", lockName)
                                .setParameter("timeoutSeconds", LOCK_TIMEOUT_SECONDS)
                                .getSingleResult();
        return acquired != null && acquired.intValue() == 1;
    }

    private void releaseUserLock(Long userId) {
        entityManager
                .createNativeQuery("SELECT RELEASE_LOCK(:lockName)")
                .setParameter("lockName", lockName(userId))
                .getSingleResult();
    }

    private String lockName(Long userId) {
        return "recommend:" + userId;
    }

    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void processFallbackJob(String jobId, Long userId, RecommendationScenario scenario) {
        try {
            issueRecommendationsOnce(userId, scenario);
            markJobDone(jobId);
            return;
        } catch (WebClientResponseException exception) {
            if (isRateLimitStatus(exception)) {
                sleepMillis(RATE_LIMIT_BACKOFF_MILLIS);
                try {
                    issueRecommendationsOnce(userId, scenario);
                    markJobDone(jobId);
                    return;
                } catch (WebClientResponseException retryException) {
                    markJobFailed(
                            jobId,
                            "RECOMMEND_HTTP_" + retryException.getStatusCode().value(),
                            summarizeError(retryException));
                    return;
                } catch (Exception retryException) {
                    markJobFailed(jobId, "RECOMMEND_INTERNAL_ERROR", summarizeError(retryException));
                    return;
                }
            }
            markJobFailed(
                    jobId, "RECOMMEND_HTTP_" + exception.getStatusCode().value(), summarizeError(exception));
            return;
        } catch (Exception exception) {
            markJobFailed(jobId, "RECOMMEND_INTERNAL_ERROR", summarizeError(exception));
            return;
        }
    }

    private void issueRecommendationsOnce(Long userId, RecommendationScenario scenario) {
        RecommendedProblemService proxy = recommendedProblemServiceProvider.getIfAvailable();
        if (proxy != null) {
            proxy.issueRecommendationsForUser(userId, scenario);
            return;
        }
        issueRecommendationsForUser(userId, scenario);
    }

    private boolean isRateLimitStatus(WebClientResponseException exception) {
        int statusCode = exception.getStatusCode().value();
        return statusCode == 429 || statusCode == 503;
    }

    private void markJobDone(String jobId) {
        recommendationJobRepository
                .findById(jobId)
                .ifPresent(
                        job -> {
                            if (job.isTerminal()) {
                                return;
                            }
                            job.markDone(Instant.now());
                            recommendationJobRepository.save(job);
                        });
    }

    private void markJobFailed(String jobId, String errorCode, String errorMessage) {
        recommendationJobRepository
                .findById(jobId)
                .ifPresent(
                        job -> {
                            if (job.isTerminal()) {
                                return;
                            }
                            job.markFailed(errorCode, errorMessage, Instant.now());
                            recommendationJobRepository.save(job);
                        });
    }

    private String summarizeError(Exception exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return null;
        }
        String message = exception.getMessage();
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    public record RecommendationCandidate(Long problemId, String reasonMsg) {}

    public record RecommendationFilterInfo(
            List<Long> solvedProblemIds, List<Long> challengeProblemIds) {}
}
