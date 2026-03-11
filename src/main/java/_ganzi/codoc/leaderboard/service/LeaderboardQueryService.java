package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.config.LeaderboardRedisProperties;
import _ganzi.codoc.leaderboard.domain.LeaderboardGroupMember;
import _ganzi.codoc.leaderboard.domain.LeaderboardPolicy;
import _ganzi.codoc.leaderboard.domain.LeaderboardScopeType;
import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshot;
import _ganzi.codoc.leaderboard.domain.LeaderboardSnapshotBatch;
import _ganzi.codoc.leaderboard.domain.League;
import _ganzi.codoc.leaderboard.exception.InvalidStartRankException;
import _ganzi.codoc.leaderboard.exception.NotLeaderboardParticipantException;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisKeyFactory;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisRepository;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisScoreCodec;
import _ganzi.codoc.leaderboard.repository.LeaderboardGroupMemberRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardPolicyRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSeasonRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSnapshotBatchRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSnapshotRepository;
import _ganzi.codoc.leaderboard.repository.LeagueRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    private final LeagueRepository leagueRepository;
    private final LeaderboardRedisProperties leaderboardRedisProperties;
    private final LeaderboardRedisKeyFactory leaderboardRedisKeyFactory;
    private final LeaderboardRedisRepository leaderboardRedisRepository;
    private final LeaderboardRedisScoreCodec leaderboardRedisScoreCodec;

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
        Integer leagueId = null;
        if (snapshotId != null) {
            leagueId =
                    snapshotRepository
                            .findScopeIdBySnapshotAndUser(snapshotId, LeaderboardScopeType.LEAGUE, userId)
                            .map(Long::intValue)
                            .orElse(null);
        }
        League league = null;
        if (leagueId != null) {
            league = leagueRepository.findById(leagueId).orElse(null);
        }
        if (league == null) {
            league = user.getLeague();
        }
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
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<UserGlobalRankResponse> redisResponse = getUserGlobalRankFromRedis(userId);
                if (redisResponse.isPresent()) {
                    return redisResponse.get();
                }
            } catch (Exception exception) {
                log.warn("leaderboard redis read failed. scope=GLOBAL, userId={}", userId, exception);
            }
        }
        return getUserGlobalRankFromSnapshot(userId);
    }

    private UserGlobalRankResponse getUserGlobalRankFromSnapshot(Long userId) {
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
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<UserLeagueRankResponse> redisResponse = getUserLeagueRankFromRedis(userId);
                if (redisResponse.isPresent()) {
                    return redisResponse.get();
                }
            } catch (Exception exception) {
                log.warn("leaderboard redis read failed. scope=LEAGUE, userId={}", userId, exception);
            }
        }
        return getUserLeagueRankFromSnapshot(userId);
    }

    private UserLeagueRankResponse getUserLeagueRankFromSnapshot(Long userId) {
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
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<UserGroupRankResponse> redisResponse = getUserGroupRankFromRedis(userId);
                if (redisResponse.isPresent()) {
                    return redisResponse.get();
                }
            } catch (Exception exception) {
                log.warn("leaderboard redis read failed. scope=GROUP, userId={}", userId, exception);
            }
        }
        return getUserGroupRankFromSnapshot(userId);
    }

    private UserGroupRankResponse getUserGroupRankFromSnapshot(Long userId) {
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
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<LeaderboardRankPageResponse> redisResponse =
                        getGlobalLeaderboardFromRedis(userId, startRank, limit);
                if (redisResponse.isPresent()) {
                    return redisResponse.get();
                }
            } catch (Exception exception) {
                log.warn(
                        "leaderboard redis read failed." + " scope=GLOBAL, userId={}, startRank={}, limit={}",
                        userId,
                        startRank,
                        limit,
                        exception);
            }
        }
        return getGlobalLeaderboardFromSnapshot(userId, startRank, limit);
    }

    private LeaderboardRankPageResponse getGlobalLeaderboardFromSnapshot(
            Long userId, int startRank, int limit) {
        ParticipantContext context = resolveParticipant(userId);
        validateRange(startRank, limit);
        int endRank = startRank + limit - 1;
        List<LeaderboardSnapshot> snapshots =
                snapshotRepository.findGlobalSnapshots(
                        context.snapshotId(), LeaderboardScopeType.GLOBAL, startRank, endRank);
        return toRankPageResponse(startRank, limit, snapshots);
    }

    public LeaderboardRankPageResponse getLeagueLeaderboard(Long userId, int startRank, int limit) {
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<LeaderboardRankPageResponse> redisResponse =
                        getLeagueLeaderboardFromRedis(userId, startRank, limit);
                if (redisResponse.isPresent()) {
                    return redisResponse.get();
                }
            } catch (Exception exception) {
                log.warn(
                        "leaderboard redis read failed." + " scope=LEAGUE, userId={}, startRank={}, limit={}",
                        userId,
                        startRank,
                        limit,
                        exception);
            }
        }
        return getLeagueLeaderboardFromSnapshot(userId, startRank, limit);
    }

    private LeaderboardRankPageResponse getLeagueLeaderboardFromSnapshot(
            Long userId, int startRank, int limit) {
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
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<LeaderboardGroupPageResponse> redisResponse =
                        getGroupLeaderboardFromRedis(userId, startRank, limit);
                if (redisResponse.isPresent()) {
                    return redisResponse.get();
                }
            } catch (Exception exception) {
                log.warn(
                        "leaderboard redis read failed." + " scope=GROUP, userId={}, startRank={}, limit={}",
                        userId,
                        startRank,
                        limit,
                        exception);
            }
        }
        return getGroupLeaderboardFromSnapshot(userId, startRank, limit);
    }

    private LeaderboardGroupPageResponse getGroupLeaderboardFromSnapshot(
            Long userId, int startRank, int limit) {
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
        LeaderboardSeason season =
                findSeasonForRead().orElseThrow(NotLeaderboardParticipantException::new);
        LeaderboardSnapshotBatch snapshotBatch =
                findLatestSnapshotBatch(season).orElseThrow(NotLeaderboardParticipantException::new);
        Integer leagueId = resolveLeagueIdFromSnapshot(userId, snapshotBatch.getId());
        League league = leagueId == null ? null : leagueRepository.findById(leagueId).orElse(null);
        if (league == null) {
            league = user.getLeague();
        }
        if (league == null) {
            throw new NotLeaderboardParticipantException();
        }
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

    private Optional<UserGlobalRankResponse> getUserGlobalRankFromRedis(Long userId) {
        Optional<RedisParticipantContext> context = resolveRedisParticipant(userId);
        if (context.isEmpty()) {
            return Optional.empty();
        }

        RedisParticipantContext participant = context.get();
        String globalKey = leaderboardRedisKeyFactory.globalKey(participant.seasonId());
        Long rank = leaderboardRedisRepository.reverseRank(globalKey, userId);
        Double score = leaderboardRedisRepository.score(globalKey, userId);
        if (rank == null || score == null) {
            return Optional.empty();
        }

        return Optional.of(
                new UserGlobalRankResponse(
                        rank.intValue() + 1,
                        leaderboardRedisScoreCodec.decodeWeeklyXp(score),
                        participant.user().getNickname(),
                        participant.user().getAvatar().getImageUrl()));
    }

    private Optional<UserLeagueRankResponse> getUserLeagueRankFromRedis(Long userId) {
        Optional<RedisParticipantContext> context = resolveRedisParticipant(userId);
        if (context.isEmpty()) {
            return Optional.empty();
        }

        RedisParticipantContext participant = context.get();
        String leagueKey =
                leaderboardRedisKeyFactory.leagueKey(
                        participant.seasonId(), participant.leagueId().intValue());
        Long rank = leaderboardRedisRepository.reverseRank(leagueKey, userId);
        Double score = leaderboardRedisRepository.score(leagueKey, userId);
        if (rank == null || score == null) {
            return Optional.empty();
        }

        return Optional.of(
                new UserLeagueRankResponse(
                        rank.intValue() + 1,
                        leaderboardRedisScoreCodec.decodeWeeklyXp(score),
                        participant.user().getNickname(),
                        participant.user().getAvatar().getImageUrl()));
    }

    private Optional<UserGroupRankResponse> getUserGroupRankFromRedis(Long userId) {
        Optional<RedisParticipantContext> context = resolveRedisParticipant(userId);
        if (context.isEmpty()) {
            return Optional.empty();
        }

        RedisParticipantContext participant = context.get();
        String groupKey =
                leaderboardRedisKeyFactory.groupKey(participant.seasonId(), participant.groupId());
        Long rank = leaderboardRedisRepository.reverseRank(groupKey, userId);
        Double score = leaderboardRedisRepository.score(groupKey, userId);
        if (rank == null || score == null) {
            return Optional.empty();
        }

        return Optional.of(
                new UserGroupRankResponse(
                        participant.groupId(),
                        rank.intValue() + 1,
                        leaderboardRedisScoreCodec.decodeWeeklyXp(score),
                        participant.user().getNickname(),
                        participant.user().getAvatar().getImageUrl()));
    }

    private Optional<LeaderboardRankPageResponse> getGlobalLeaderboardFromRedis(
            Long userId, int startRank, int limit) {
        validateRange(startRank, limit);
        Optional<RedisParticipantContext> context = resolveRedisParticipant(userId);
        if (context.isEmpty()) {
            return Optional.empty();
        }
        RedisParticipantContext participant = context.get();
        String globalKey = leaderboardRedisKeyFactory.globalKey(participant.seasonId());
        LeaderboardRankPageResponse page =
                readRankPageFromRedis(globalKey, globalKey, startRank, limit);
        if (startRank == 1 && page.ranks().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(page);
    }

    private Optional<LeaderboardRankPageResponse> getLeagueLeaderboardFromRedis(
            Long userId, int startRank, int limit) {
        validateRange(startRank, limit);
        Optional<RedisParticipantContext> context = resolveRedisParticipant(userId);
        if (context.isEmpty()) {
            return Optional.empty();
        }
        RedisParticipantContext participant = context.get();
        String globalKey = leaderboardRedisKeyFactory.globalKey(participant.seasonId());
        String leagueKey =
                leaderboardRedisKeyFactory.leagueKey(
                        participant.seasonId(), participant.leagueId().intValue());
        LeaderboardRankPageResponse page =
                readRankPageFromRedis(leagueKey, globalKey, startRank, limit);
        if (startRank == 1 && page.ranks().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(page);
    }

    private Optional<LeaderboardGroupPageResponse> getGroupLeaderboardFromRedis(
            Long userId, int startRank, int limit) {
        validateRange(startRank, limit);
        Optional<RedisParticipantContext> context = resolveRedisParticipant(userId);
        if (context.isEmpty()) {
            return Optional.empty();
        }
        RedisParticipantContext participant = context.get();
        String globalKey = leaderboardRedisKeyFactory.globalKey(participant.seasonId());
        String groupKey =
                leaderboardRedisKeyFactory.groupKey(participant.seasonId(), participant.groupId());
        LeaderboardRankPageResponse page = readRankPageFromRedis(groupKey, globalKey, startRank, limit);
        if (startRank == 1 && page.ranks().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                new LeaderboardGroupPageResponse(
                        participant.seasonId(),
                        participant.groupId(),
                        null,
                        page.startRank(),
                        page.endRank(),
                        page.hasMore(),
                        page.ranks()));
    }

    private LeaderboardRankPageResponse readRankPageFromRedis(
            String scoreKey, String globalKey, int startRank, int limit) {
        long start = startRank - 1L;
        long end = start + limit - 1L;

        List<ZSetOperations.TypedTuple<String>> tuples =
                leaderboardRedisRepository.top(scoreKey, start, end);
        if (tuples.isEmpty()) {
            return new LeaderboardRankPageResponse(startRank, startRank - 1, false, List.of());
        }

        List<Long> userIds = new ArrayList<>(tuples.size());
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() != null) {
                userIds.add(Long.parseLong(tuple.getValue()));
            }
        }

        Map<Long, User> userById = new HashMap<>();
        for (User user : userRepository.findAllById(userIds)) {
            userById.put(user.getId(), user);
        }

        List<LeaderboardRankItem> ranks = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }
            Long userId = Long.parseLong(tuple.getValue());
            User user = userById.get(userId);
            if (user == null) {
                continue;
            }

            Long globalRank = leaderboardRedisRepository.reverseRank(globalKey, userId);
            if (globalRank == null) {
                continue;
            }

            ranks.add(
                    new LeaderboardRankItem(
                            globalRank.intValue() + 1,
                            userId,
                            user.getAvatar().getImageUrl(),
                            user.getNickname(),
                            leaderboardRedisScoreCodec.decodeWeeklyXp(tuple.getScore())));
        }

        int endRank = ranks.isEmpty() ? startRank - 1 : startRank + ranks.size() - 1;
        boolean hasMore = tuples.size() == limit;
        return new LeaderboardRankPageResponse(startRank, endRank, hasMore, ranks);
    }

    private Optional<RedisParticipantContext> resolveRedisParticipant(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getLeague() == null) {
            throw new NotLeaderboardParticipantException();
        }

        Optional<LeaderboardSeason> currentSeason = findCurrentSeason();
        if (currentSeason.isEmpty()) {
            return Optional.empty();
        }

        LeaderboardGroupMember member =
                groupMemberRepository
                        .findFirstBySeasonIdAndUserId(currentSeason.get().getSeasonId(), userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);

        return Optional.of(
                new RedisParticipantContext(
                        currentSeason.get().getSeasonId(),
                        user.getLeague().getId().longValue(),
                        member.getGroup().getId(),
                        user));
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

    private Optional<LeaderboardSeason> findCurrentSeason() {
        Instant now = Instant.now();
        return seasonRepository.findFirstByStartsAtLessThanEqualAndEndsAtAfterOrderByStartsAtDesc(
                now, now);
    }

    private Optional<LeaderboardSnapshotBatch> findLatestSnapshotBatch(LeaderboardSeason season) {
        return snapshotBatchRepository.findFirstBySeasonIdOrderByIdDesc(season.getSeasonId());
    }

    private void validateRange(int startRank, int limit) {
        if (startRank < 1 || limit < 1) {
            throw new InvalidStartRankException();
        }
    }

    private Integer resolveLeagueIdFromSnapshot(Long userId, Long snapshotId) {
        if (snapshotId == null) {
            return null;
        }
        return snapshotRepository
                .findScopeIdBySnapshotAndUser(snapshotId, LeaderboardScopeType.LEAGUE, userId)
                .map(Long::intValue)
                .orElse(null);
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

    private record RedisParticipantContext(
            Integer seasonId, Long leagueId, Long groupId, User user) {}
}
