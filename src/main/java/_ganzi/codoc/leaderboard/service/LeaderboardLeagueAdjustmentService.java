package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.domain.LeaderboardPolicy;
import _ganzi.codoc.leaderboard.domain.LeaderboardScopeType;
import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshot;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshotBatch;
import _ganzi.codoc.leaderboard.domain.League;
import _ganzi.codoc.leaderboard.repository.LeaderboardGroupRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardPolicyRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSeasonRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSnapshotBatchRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSnapshotRepository;
import _ganzi.codoc.leaderboard.repository.LeagueRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.enums.UserStatus;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardLeagueAdjustmentService {

    private static final int PROMOTION_MIN_WEEKLY_XP = 0;

    private final LeaderboardSeasonRepository seasonRepository;
    private final LeaderboardSnapshotBatchRepository snapshotBatchRepository;
    private final LeaderboardSnapshotRepository snapshotRepository;
    private final LeaderboardGroupRepository groupRepository;
    private final LeagueRepository leagueRepository;
    private final LeaderboardPolicyRepository policyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void adjustLeaguesForSeasonEnd() {
        LeaderboardSeason season = findLatestEndedSeason().orElse(null);
        if (season == null) {
            return;
        }
        Integer seasonId = season.getSeasonId();
        LeaderboardSnapshotBatch snapshotBatch =
                snapshotBatchRepository.findFirstBySeasonIdOrderByIdDesc(seasonId).orElse(null);
        if (snapshotBatch == null) {
            return;
        }

        List<League> leagues = leagueRepository.findAllByIsActiveTrueOrderBySortOrderAsc();
        if (leagues.isEmpty()) {
            return;
        }
        Map<Integer, LeaderboardPolicy> policyByLeagueId =
                policyRepository.findAll().stream()
                        .collect(Collectors.toMap(policy -> policy.getLeague().getId(), policy -> policy));

        Map<Long, LeagueTarget> targets = new HashMap<>();
        for (int index = 0; index < leagues.size(); index++) {
            League league = leagues.get(index);
            LeaderboardPolicy policy = policyByLeagueId.get(league.getId());
            if (policy == null) {
                continue;
            }
            League higherLeague = index + 1 < leagues.size() ? leagues.get(index + 1) : null;
            League lowerLeague = index - 1 >= 0 ? leagues.get(index - 1) : null;

            if (higherLeague != null && policy.getPromoteTopN() > 0) {
                registerPromotionTargets(
                        targets,
                        snapshotBatch.getId(),
                        seasonId,
                        league,
                        higherLeague,
                        policy.getPromoteTopN());
            }

            if (lowerLeague != null && policy.getDemoteBottomN() > 0) {
                registerDemotionTargets(
                        targets,
                        snapshotBatch.getId(),
                        seasonId,
                        league,
                        lowerLeague,
                        policy.getDemoteBottomN());
            }
        }

        if (targets.isEmpty()) {
            return;
        }

        Set<Long> userIds = targets.keySet();
        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            LeagueTarget target = targets.get(user.getId());
            if (target == null) {
                continue;
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                continue;
            }
            if (user.getLeague() == null) {
                continue;
            }
            if (!user.getLeague().getId().equals(target.sourceLeagueId())) {
                continue;
            }
            user.updateLeague(target.targetLeague());
        }
    }

    private Optional<LeaderboardSeason> findLatestEndedSeason() {
        Instant now = Instant.now();
        return seasonRepository.findFirstByEndsAtLessThanEqualOrderByEndsAtDesc(now);
    }

    private void registerPromotionTargets(
            Map<Long, LeagueTarget> targets,
            Long snapshotId,
            Integer seasonId,
            League sourceLeague,
            League targetLeague,
            int promoteTopN) {
        List<Long> groupIds =
                groupRepository.findIdsBySeasonIdAndLeagueId(seasonId, sourceLeague.getId());
        for (Long groupId : groupIds) {
            List<LeaderboardSnapshot> snapshots =
                    snapshotRepository
                            .findBySnapshotBatchIdAndScopeTypeAndScopeIdAndWeeklyXpGreaterThanOrderByRankAsc(
                                    snapshotId,
                                    LeaderboardScopeType.GROUP,
                                    groupId,
                                    PROMOTION_MIN_WEEKLY_XP,
                                    PageRequest.of(0, promoteTopN));
            for (LeaderboardSnapshot snapshot : snapshots) {
                targets.putIfAbsent(
                        snapshot.getUser().getId(), new LeagueTarget(sourceLeague.getId(), targetLeague));
            }
        }
    }

    private void registerDemotionTargets(
            Map<Long, LeagueTarget> targets,
            Long snapshotId,
            Integer seasonId,
            League sourceLeague,
            League targetLeague,
            int demoteBottomN) {
        List<Long> groupIds =
                groupRepository.findIdsBySeasonIdAndLeagueId(seasonId, sourceLeague.getId());
        for (Long groupId : groupIds) {
            List<LeaderboardSnapshot> snapshots =
                    snapshotRepository.findBySnapshotBatchIdAndScopeTypeAndScopeIdOrderByRankDesc(
                            snapshotId, LeaderboardScopeType.GROUP, groupId, PageRequest.of(0, demoteBottomN));
            for (LeaderboardSnapshot snapshot : snapshots) {
                targets.putIfAbsent(
                        snapshot.getUser().getId(), new LeagueTarget(sourceLeague.getId(), targetLeague));
            }
        }
    }

    private record LeagueTarget(Integer sourceLeagueId, League targetLeague) {}
}
