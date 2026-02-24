package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.domain.LeaderboardGroupMember;
import _ganzi.codoc.leaderboard.domain.LeaderboardPolicy;
import _ganzi.codoc.leaderboard.domain.LeaderboardScopeType;
import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshot;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshotBatch;
import _ganzi.codoc.leaderboard.domain.League;
import _ganzi.codoc.leaderboard.exception.InvalidStartRankException;
import _ganzi.codoc.leaderboard.exception.NotLeaderboardParticipantException;
import _ganzi.codoc.leaderboard.repository.LeaderboardGroupMemberRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardPolicyRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSeasonRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSnapshotBatchRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSnapshotRepository;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardGroupPageResponse;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardRankItem;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardRankPageResponse;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardSeasonResponse;
import _ganzi.codoc.leaderboard.service.dto.UserGlobalRankResponse;
import _ganzi.codoc.leaderboard.service.dto.UserGroupRankResponse;
import _ganzi.codoc.leaderboard.service.dto.UserLeagueInfoResponse;
import _ganzi.codoc.leaderboard.service.dto.UserLeagueRankResponse;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardQueryService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final LeaderboardSeasonRepository seasonRepository;
    private final LeaderboardSnapshotBatchRepository snapshotBatchRepository;
    private final LeaderboardSnapshotRepository snapshotRepository;
    private final LeaderboardGroupMemberRepository groupMemberRepository;
    private final LeaderboardPolicyRepository policyRepository;

    public UserLeagueInfoResponse getUserLeagueInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Optional<LeaderboardSeason> season = findSeasonForRead();
        Integer seasonId = season.map(LeaderboardSeason::getSeasonId).orElse(null);
        Long snapshotId =
                season
                        .flatMap(this::findLatestSnapshotBatch)
                        .map(LeaderboardSnapshotBatch::getId)
                        .orElse(null);
        Long groupId = null;
        if (seasonId != null) {
            groupId =
                    groupMemberRepository
                            .findFirstBySeasonIdAndUserId(seasonId, userId)
                            .map(member -> member.getGroup().getId())
                            .orElse(null);
        }
        League league = user.getLeague();
        LeaderboardPolicy policy =
                league == null ? null : policyRepository.findByLeagueId(league.getId()).orElse(null);
        return new UserLeagueInfoResponse(
                seasonId,
                snapshotId,
                league == null ? null : league.getId(),
                league == null ? null : league.getName(),
                league == null ? null : league.getLogoUrl(),
                groupId,
                policy == null ? null : policy.getPromoteTopN(),
                policy == null ? null : policy.getDemoteBottomN());
    }

    public LeaderboardSeasonResponse getCurrentSeason() {
        return findSeasonForRead()
                .map(
                        season ->
                                new LeaderboardSeasonResponse(
                                        season.getSeasonId(),
                                        toLocalDate(season.getStartsAt()),
                                        toLocalDate(season.getEndsAt())))
                .orElse(null);
    }

    public UserGlobalRankResponse getUserGlobalRank(Long userId) {
        ParticipantContext context = resolveParticipant(userId);
        LeaderboardSnapshot snapshot =
                snapshotRepository
                        .findGlobalSnapshotByUser(context.snapshotId(), LeaderboardScopeType.GLOBAL, userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);
        return new UserGlobalRankResponse(
                snapshot.getRank(),
                snapshot.getWeeklyXp(),
                snapshot.getUser().getNickname(),
                snapshot.getUser().getAvatar().getImageUrl());
    }

    public UserLeagueRankResponse getUserLeagueRank(Long userId) {
        ParticipantContext context = resolveParticipant(userId);
        LeaderboardSnapshot snapshot =
                snapshotRepository
                        .findScopedSnapshotByUser(
                                context.snapshotId(), LeaderboardScopeType.LEAGUE, context.leagueId(), userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);
        return new UserLeagueRankResponse(
                snapshot.getRank(),
                snapshot.getWeeklyXp(),
                snapshot.getUser().getNickname(),
                snapshot.getUser().getAvatar().getImageUrl());
    }

    public UserGroupRankResponse getUserGroupRank(Long userId) {
        ParticipantContext context = resolveParticipant(userId);
        LeaderboardSnapshot snapshot =
                snapshotRepository
                        .findScopedSnapshotByUser(
                                context.snapshotId(), LeaderboardScopeType.GROUP, context.groupId(), userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);
        return new UserGroupRankResponse(
                context.groupId(),
                snapshot.getRank(),
                snapshot.getWeeklyXp(),
                snapshot.getUser().getNickname(),
                snapshot.getUser().getAvatar().getImageUrl());
    }

    public LeaderboardRankPageResponse getGlobalLeaderboard(Long userId, int startRank, int limit) {
        ParticipantContext context = resolveParticipant(userId);
        validateRange(startRank, limit);
        int endRank = startRank + limit - 1;
        List<LeaderboardSnapshot> snapshots =
                snapshotRepository.findGlobalSnapshots(
                        context.snapshotId(), LeaderboardScopeType.GLOBAL, startRank, endRank);
        return toRankPageResponse(startRank, limit, snapshots);
    }

    public LeaderboardRankPageResponse getLeagueLeaderboard(Long userId, int startRank, int limit) {
        ParticipantContext context = resolveParticipant(userId);
        validateRange(startRank, limit);
        int endRank = startRank + limit - 1;
        List<LeaderboardSnapshot> snapshots =
                snapshotRepository.findScopedSnapshots(
                        context.snapshotId(),
                        LeaderboardScopeType.LEAGUE,
                        context.leagueId(),
                        startRank,
                        endRank);
        return toRankPageResponse(startRank, limit, snapshots);
    }

    public LeaderboardGroupPageResponse getGroupLeaderboard(Long userId, int startRank, int limit) {
        ParticipantContext context = resolveParticipant(userId);
        validateRange(startRank, limit);
        int endRank = startRank + limit - 1;
        List<LeaderboardSnapshot> snapshots =
                snapshotRepository.findScopedSnapshots(
                        context.snapshotId(),
                        LeaderboardScopeType.GROUP,
                        context.groupId(),
                        startRank,
                        endRank);
        LeaderboardRankPageResponse page = toRankPageResponse(startRank, limit, snapshots);
        return new LeaderboardGroupPageResponse(
                context.seasonId(),
                context.groupId(),
                context.snapshotId(),
                page.startRank(),
                page.endRank(),
                page.hasMore(),
                page.ranks());
    }

    private ParticipantContext resolveParticipant(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        League league = user.getLeague();
        if (league == null) {
            throw new NotLeaderboardParticipantException();
        }
        LeaderboardSeason season =
                findSeasonForRead().orElseThrow(NotLeaderboardParticipantException::new);
        LeaderboardSnapshotBatch snapshotBatch =
                findLatestSnapshotBatch(season).orElseThrow(NotLeaderboardParticipantException::new);
        LeaderboardGroupMember member =
                groupMemberRepository
                        .findFirstBySeasonIdAndUserId(season.getSeasonId(), userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);
        return new ParticipantContext(
                season.getSeasonId(),
                snapshotBatch.getId(),
                league.getId().longValue(),
                member.getGroup().getId());
    }

    private Optional<LeaderboardSeason> findSeasonForRead() {
        Instant now = Instant.now();
        Optional<LeaderboardSeason> current =
                seasonRepository.findFirstByStartsAtLessThanEqualAndEndsAtAfterOrderByStartsAtDesc(
                        now, now);
        if (current.isPresent()) {
            return current;
        }
        return seasonRepository.findFirstByEndsAtBeforeOrderByEndsAtDesc(now);
    }

    private Optional<LeaderboardSnapshotBatch> findLatestSnapshotBatch(LeaderboardSeason season) {
        return snapshotBatchRepository.findFirstBySeasonIdOrderByIdDesc(season.getSeasonId());
    }

    private void validateRange(int startRank, int limit) {
        if (startRank < 1 || limit < 1) {
            throw new InvalidStartRankException();
        }
    }

    private LeaderboardRankPageResponse toRankPageResponse(
            int startRank, int limit, List<LeaderboardSnapshot> snapshots) {
        List<LeaderboardRankItem> ranks = new ArrayList<>();
        for (LeaderboardSnapshot snapshot : snapshots) {
            ranks.add(
                    new LeaderboardRankItem(
                            snapshot.getRank(),
                            snapshot.getUser().getId(),
                            snapshot.getUser().getAvatar().getImageUrl(),
                            snapshot.getUser().getNickname(),
                            snapshot.getWeeklyXp()));
        }
        int endRank = ranks.isEmpty() ? startRank - 1 : startRank + ranks.size() - 1;
        boolean hasMore = ranks.size() == limit;
        return new LeaderboardRankPageResponse(startRank, endRank, hasMore, ranks);
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(SEOUL).toLocalDate();
    }

    private record ParticipantContext(
            Integer seasonId, Long snapshotId, Long leagueId, Long groupId) {}
}
