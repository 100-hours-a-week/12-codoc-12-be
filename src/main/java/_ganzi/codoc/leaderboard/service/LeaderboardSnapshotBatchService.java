package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.domain.LeaderboardScopeType;
import _ganzi.codoc.leaderboard.domain.LeaderboardScore;
import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshot;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshotBatch;
import _ganzi.codoc.leaderboard.repository.LeaderboardScoreRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSeasonRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSnapshotBatchRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSnapshotRepository;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.service.NotificationDispatchService;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardSnapshotBatchService {

    private static final int NOTIFICATION_DISPATCH_BATCH_SIZE = 500;
    private static final String SEASON_START_TITLE = "리더보드 새 시즌이 시작됐어요";
    private static final String SEASON_START_BODY = "이번 시즌 랭킹을 확인하고 도전해보세요.";
    private static final String SEASON_END_TITLE = "리더보드 시즌이 종료됐어요";
    private static final String SEASON_END_BODY = "최종 결과를 확인해보세요.";

    private final LeaderboardSeasonRepository seasonRepository;
    private final LeaderboardScoreRepository scoreRepository;
    private final LeaderboardSnapshotBatchRepository snapshotBatchRepository;
    private final LeaderboardSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final NotificationDispatchService notificationDispatchService;

    @Transactional
    public void createHourlySnapshot() {
        LeaderboardSeason season = findCurrentSeason().orElse(null);
        if (season == null) {
            return;
        }
        createSnapshotForSeason(season);
    }

    @Transactional
    public void createSeasonStartSnapshot() {
        LeaderboardSeason season = findCurrentSeason().orElse(null);
        if (season == null) {
            return;
        }
        createSnapshotForSeason(season);
        notifyActiveUsers(
                NotificationType.LEADERBOARD_STARTED,
                SEASON_START_TITLE,
                SEASON_START_BODY,
                Map.of("seasonId", String.valueOf(season.getSeasonId())));
    }

    @Transactional
    public void createSeasonEndSnapshot() {
        LeaderboardSeason season = findLatestEndedSeason().orElse(null);
        if (season == null) {
            return;
        }
        createSnapshotForSeason(season);
        notifyActiveUsers(
                NotificationType.LEADERBOARD_CLOSED,
                SEASON_END_TITLE,
                SEASON_END_BODY,
                Map.of("seasonId", String.valueOf(season.getSeasonId())));
    }

    private void notifyActiveUsers(
            NotificationType type, String title, String body, Map<String, String> linkParams) {
        List<Long> userIds = userRepository.findAllActiveUserIds();
        NotificationMessageItem messageItem =
                new NotificationMessageItem(type, title, body, linkParams);
        notificationDispatchService.dispatchBatchAfterCommit(
                userIds, messageItem, NOTIFICATION_DISPATCH_BATCH_SIZE);
    }

    private void createSnapshotForSeason(LeaderboardSeason season) {
        List<LeaderboardScore> scores = scoreRepository.findAllByIdSeasonId(season.getSeasonId());
        if (scores.isEmpty()) {
            return;
        }
        LeaderboardSnapshotBatch batch =
                snapshotBatchRepository.save(LeaderboardSnapshotBatch.create(season.getSeasonId()));
        Instant createdAt = Instant.now();
        List<LeaderboardSnapshot> snapshots = new ArrayList<>();
        Comparator<LeaderboardScore> comparator = scoreComparator();

        List<LeaderboardScore> global = new ArrayList<>(scores);
        global.sort(comparator);
        appendSnapshots(
                snapshots,
                batch,
                season.getSeasonId(),
                LeaderboardScopeType.GLOBAL,
                null,
                global,
                createdAt);

        Map<Integer, List<LeaderboardScore>> byLeague = new HashMap<>();
        for (LeaderboardScore score : scores) {
            byLeague.computeIfAbsent(score.getLeague().getId(), key -> new ArrayList<>()).add(score);
        }
        for (Map.Entry<Integer, List<LeaderboardScore>> entry : byLeague.entrySet()) {
            List<LeaderboardScore> leagueScores = entry.getValue();
            leagueScores.sort(comparator);
            appendSnapshots(
                    snapshots,
                    batch,
                    season.getSeasonId(),
                    LeaderboardScopeType.LEAGUE,
                    entry.getKey().longValue(),
                    leagueScores,
                    createdAt);
        }

        Map<Long, List<LeaderboardScore>> byGroup = new HashMap<>();
        for (LeaderboardScore score : scores) {
            Long groupId = score.getGroupId();
            if (groupId == null) {
                continue;
            }
            byGroup.computeIfAbsent(groupId, key -> new ArrayList<>()).add(score);
        }
        for (Map.Entry<Long, List<LeaderboardScore>> entry : byGroup.entrySet()) {
            List<LeaderboardScore> groupScores = entry.getValue();
            groupScores.sort(comparator);
            appendSnapshots(
                    snapshots,
                    batch,
                    season.getSeasonId(),
                    LeaderboardScopeType.GROUP,
                    entry.getKey(),
                    groupScores,
                    createdAt);
        }

        snapshotRepository.saveAll(snapshots);
    }

    private java.util.Optional<LeaderboardSeason> findCurrentSeason() {
        Instant now = Instant.now();
        return seasonRepository.findFirstByStartsAtLessThanEqualAndEndsAtAfterOrderByStartsAtDesc(
                now, now);
    }

    private java.util.Optional<LeaderboardSeason> findLatestEndedSeason() {
        Instant now = Instant.now();
        return seasonRepository.findFirstByEndsAtLessThanEqualOrderByEndsAtDesc(now);
    }

    private Comparator<LeaderboardScore> scoreComparator() {
        return Comparator.comparingInt(LeaderboardScore::getWeeklyXp)
                .reversed()
                .thenComparing(LeaderboardScore::getUpdatedAt)
                .thenComparing(score -> score.getUser().getId());
    }

    private void appendSnapshots(
            List<LeaderboardSnapshot> snapshots,
            LeaderboardSnapshotBatch batch,
            Integer seasonId,
            LeaderboardScopeType scopeType,
            Long scopeId,
            List<LeaderboardScore> scores,
            Instant createdAt) {
        int rank = 1;
        for (LeaderboardScore score : scores) {
            snapshots.add(
                    LeaderboardSnapshot.create(
                            batch,
                            seasonId,
                            scopeType,
                            scopeId,
                            score.getUser(),
                            rank++,
                            score.getWeeklyXp(),
                            score.getUpdatedAt(),
                            createdAt));
        }
    }
}
