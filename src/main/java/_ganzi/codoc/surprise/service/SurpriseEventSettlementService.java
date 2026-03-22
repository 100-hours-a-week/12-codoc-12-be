package _ganzi.codoc.surprise.service;

import _ganzi.codoc.leaderboard.service.LeaderboardScoreService;
import _ganzi.codoc.surprise.domain.SurpriseEvent;
import _ganzi.codoc.surprise.domain.SurpriseEventRewardLog;
import _ganzi.codoc.surprise.domain.SurpriseEventStatus;
import _ganzi.codoc.surprise.domain.SurpriseQuizSubmission;
import _ganzi.codoc.surprise.exception.SurpriseEventNotFoundException;
import _ganzi.codoc.surprise.repository.SurpriseEventRepository;
import _ganzi.codoc.surprise.repository.SurpriseEventRewardLogRepository;
import _ganzi.codoc.surprise.repository.SurpriseQuizSubmissionRepository;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserStatsRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SurpriseEventSettlementService {

    private static final String REWARD_TYPE_XP = "XP";
    private static final int WRONG_ANSWER_XP = 10;

    private final SurpriseEventRepository surpriseEventRepository;
    private final SurpriseQuizSubmissionRepository surpriseQuizSubmissionRepository;
    private final SurpriseEventRewardLogRepository surpriseEventRewardLogRepository;
    private final UserStatsRepository userStatsRepository;
    private final LeaderboardScoreService leaderboardScoreService;

    @Transactional
    public void closeExpiredOpenEventsAndSettle() {
        Instant now = Instant.now();
        List<SurpriseEvent> openTargets =
                surpriseEventRepository.findAllByStatusAndEndsAtLessThanEqualAndSettledAtIsNull(
                        SurpriseEventStatus.OPEN, now);
        List<SurpriseEvent> closedTargets =
                surpriseEventRepository.findAllByStatusAndEndsAtLessThanEqualAndSettledAtIsNull(
                        SurpriseEventStatus.CLOSED, now);

        for (SurpriseEvent event : openTargets) {
            settleEvent(event.getId(), now);
        }
        for (SurpriseEvent event : closedTargets) {
            settleEvent(event.getId(), now);
        }
    }

    @Transactional
    public void settleEvent(Long eventId, Instant now) {
        SurpriseEvent event =
                surpriseEventRepository
                        .findByIdForUpdate(eventId)
                        .orElseThrow(SurpriseEventNotFoundException::new);
        if (event.isSettled()) {
            return;
        }
        if (!event.isEnded(now)) {
            return;
        }
        if (event.getStatus() == SurpriseEventStatus.OPEN) {
            event.close();
        }
        if (event.getStatus() != SurpriseEventStatus.CLOSED) {
            return;
        }

        List<SurpriseQuizSubmission> correctSubmissions =
                surpriseQuizSubmissionRepository
                        .findAllByEventIdAndCorrectTrueOrderByElapsedMillisAscSubmittedAtAscUserIdAsc(eventId);
        Map<Long, Integer> rankByUserId = new HashMap<>();
        for (int i = 0; i < correctSubmissions.size(); i++) {
            SurpriseQuizSubmission submission = correctSubmissions.get(i);
            int rank = i + 1;
            submission.assignRank(rank);
            rankByUserId.put(submission.getUser().getId(), rank);
        }

        List<SurpriseQuizSubmission> allSubmissions =
                surpriseQuizSubmissionRepository.findAllByEventId(eventId);
        for (SurpriseQuizSubmission submission : allSubmissions) {
            Long userId = submission.getUser().getId();
            Integer rank = rankByUserId.get(userId);
            int rewardXp = resolveRewardXp(submission.isCorrect(), rank);
            submission.assignRank(rank);
            submission.assignEarnedXp(rewardXp);

            String idempotencyKey = rewardIdempotencyKey(eventId, userId);
            if (surpriseEventRewardLogRepository.existsByIdempotencyKey(idempotencyKey)) {
                continue;
            }

            var userStats = userStatsRepository.findById(userId).orElseThrow(UserNotFoundException::new);
            userStats.addXp(rewardXp);
            leaderboardScoreService.addWeeklyXp(userId, rewardXp);

            SurpriseEventRewardLog rewardLog =
                    SurpriseEventRewardLog.create(
                            event, submission.getUser(), REWARD_TYPE_XP, rewardXp, idempotencyKey);
            surpriseEventRewardLogRepository.save(rewardLog);
        }

        event.markSettled(now);
        event.getQuizPool().markUsed();
        log.info(
                "surprise event settled. eventId={}, submissions={}, correct={}",
                eventId,
                allSubmissions.size(),
                correctSubmissions.size());
    }

    private int resolveRewardXp(boolean correct, Integer rank) {
        if (!correct) {
            return WRONG_ANSWER_XP;
        }
        if (rank == null) {
            return 50;
        }
        if (rank == 1) {
            return 300;
        }
        if (rank == 2) {
            return 220;
        }
        if (rank == 3) {
            return 180;
        }
        if (rank <= 10) {
            return 120;
        }
        if (rank <= 30) {
            return 80;
        }
        return 50;
    }

    private String rewardIdempotencyKey(Long eventId, Long userId) {
        return eventId + ":" + userId + ":" + REWARD_TYPE_XP;
    }
}
