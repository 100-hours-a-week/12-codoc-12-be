package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.RecommendedProblem;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.RecommendedProblemRepository;
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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendedProblemService {

    private static final int REPLENISH_THRESHOLD = 1;
    private static final int BATCH_SIZE = 5;
    private static final int LOCK_TIMEOUT_SECONDS = 2;

    private final RecommendedProblemRepository recommendedProblemRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final UserProblemResultRepository userProblemResultRepository;
    private final EntityManager entityManager;
    private final RecommendClient recommendClient;

    @Transactional
    public void issueDailyRecommendations() {
        List<Long> userIds =
                userRepository.findAllByStatus(UserStatus.ACTIVE).stream().map(User::getId).toList();
        for (Long userId : userIds) {
            issueRecommendationsForUser(userId, RecommendationScenario.DAILY);
        }
    }

    @Transactional
    public void replenishIfNeeded(Long userId) {
        long pendingCount = recommendedProblemRepository.countByUserIdAndIsDoneFalse(userId);
        if (pendingCount > REPLENISH_THRESHOLD) {
            return;
        }
        issueRecommendationsForUser(userId, RecommendationScenario.ON_DEMAND);
    }

    @Transactional
    public void issueRecommendationsForUser(Long userId, RecommendationScenario scenario) {
        if (!acquireUserLock(userId)) {
            return;
        }
        try {
            long pendingCount = recommendedProblemRepository.countByUserIdAndIsDoneFalse(userId);
            if (scenario == RecommendationScenario.ON_DEMAND && pendingCount > REPLENISH_THRESHOLD) {
                return;
            }

            RecommendationFilterInfo filterInfo = buildRecommendationFilterInfo(userId, scenario);
            List<RecommendationCandidate> candidates = fetchRecommendations(userId, scenario, filterInfo);
            if (candidates.isEmpty()) {
                return;
            }

            if (scenario == RecommendationScenario.DAILY) {
                recommendedProblemRepository.markAllDoneByUserId(userId);
            }

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
            }
        } finally {
            releaseUserLock(userId);
        }
    }

    @Transactional
    public void markProblemSolved(Long userId, Long problemId) {
        int updated =
                recommendedProblemRepository.markDoneByUserIdAndProblemId(userId, problemId, Instant.now());
        if (updated > 0) {
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
    }

    private List<RecommendationCandidate> fetchRecommendations(
            Long userId, RecommendationScenario scenario, RecommendationFilterInfo filterInfo) {
        InitLevel userLevel =
                userRepository
                        .findById(userId)
                        .map(User::getInitLevel)
                        .orElseThrow(UserNotFoundException::new);
        RecommendRequest request = RecommendRequest.of(userId, userLevel, scenario, filterInfo);
        RecommendResponse response = recommendClient.requestRecommendations(request);
        return response.recommendations().stream()
                .map(
                        recommendation ->
                                new RecommendationCandidate(recommendation.problemId(), recommendation.reasonMsg()))
                .toList();
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

    public record RecommendationCandidate(Long problemId, String reasonMsg) {}

    public record RecommendationFilterInfo(
            List<Long> solvedProblemIds, List<Long> challengeProblemIds) {}
}
