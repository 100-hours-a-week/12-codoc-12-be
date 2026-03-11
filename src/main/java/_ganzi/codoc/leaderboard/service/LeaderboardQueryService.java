package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.config.LeaderboardRedisProperties;
import _ganzi.codoc.leaderboard.domain.LeaderboardGroupMember;
import _ganzi.codoc.leaderboard.domain.LeaderboardPolicy;
import _ganzi.codoc.leaderboard.domain.LeaderboardScopeType;
import _ganzi.codoc.leaderboard.domain.LeaderboardScore;
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
import _ganzi.codoc.leaderboard.repository.LeaderboardScoreRepository;
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
    private final LeaderboardScoreRepository scoreRepository;
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
        if (snapshotId != null) {
            groupId =
                    snapshotRepository
                            .findScopeIdBySnapshotAndUser(snapshotId, LeaderboardScopeType.GROUP, userId)
                            .orElse(null);
        }
        if (groupId == null && seasonId != null) {
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
        Optional<LeaderboardSeason> currentSeason = findCurrentSeason();
        if (currentSeason.isPresent()) {
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
            return getUserGlobalRankFromScore(userId, currentSeason.get());
        }
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<UserGlobalRankResponse> redisResponse =
                        getUserGlobalRankFromEndedSeasonRedis(userId);
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
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
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
        Optional<LeaderboardSeason> currentSeason = findCurrentSeason();
        if (currentSeason.isPresent()) {
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
            return getUserLeagueRankFromScore(userId, currentSeason.get());
        }
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<UserLeagueRankResponse> redisResponse =
                        getUserLeagueRankFromEndedSeasonRedis(userId);
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
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
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
        Optional<LeaderboardSeason> currentSeason = findCurrentSeason();
        if (currentSeason.isPresent()) {
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
            return getUserGroupRankFromScore(userId, currentSeason.get());
        }
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<UserGroupRankResponse> redisResponse =
                        getUserGroupRankFromEndedSeasonRedis(userId);
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
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
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
        Optional<LeaderboardSeason> currentSeason = findCurrentSeason();
        if (currentSeason.isPresent()) {
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
            return getGlobalLeaderboardFromScore(userId, startRank, limit, currentSeason.get());
        }
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<LeaderboardRankPageResponse> redisResponse =
                        getGlobalLeaderboardFromEndedSeasonRedis(userId, startRank, limit);
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
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
        validateRange(startRank, limit);
        int endRank = startRank + limit - 1;
        List<LeaderboardSnapshot> snapshots =
                snapshotRepository.findGlobalSnapshots(
                        context.snapshotId(), LeaderboardScopeType.GLOBAL, startRank, endRank);
        return toRankPageResponse(startRank, limit, snapshots);
    }

    public LeaderboardRankPageResponse getLeagueLeaderboard(Long userId, int startRank, int limit) {
        Optional<LeaderboardSeason> currentSeason = findCurrentSeason();
        if (currentSeason.isPresent()) {
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
            return getLeagueLeaderboardFromScore(userId, startRank, limit, currentSeason.get());
        }
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<LeaderboardRankPageResponse> redisResponse =
                        getLeagueLeaderboardFromEndedSeasonRedis(userId, startRank, limit);
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
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
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
        Optional<LeaderboardSeason> currentSeason = findCurrentSeason();
        if (currentSeason.isPresent()) {
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
            return getGroupLeaderboardFromScore(userId, startRank, limit, currentSeason.get());
        }
        if (leaderboardRedisProperties.readEnabled()) {
            try {
                Optional<LeaderboardGroupPageResponse> redisResponse =
                        getGroupLeaderboardFromEndedSeasonRedis(userId, startRank, limit);
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
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
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

    private UserGlobalRankResponse getUserGlobalRankFromScore(Long userId, LeaderboardSeason season) {
        InSeasonParticipantContext context = resolveInSeasonParticipant(userId, season);
        LeaderboardScore score =
                scoreRepository
                        .findByIdSeasonIdAndIdUserId(context.seasonId(), userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);
        long rank = scoreRepository.findGlobalRank(context.seasonId(), score.getWeeklyXp(), userId);
        return new UserGlobalRankResponse(
                Math.toIntExact(rank),
                score.getWeeklyXp(),
                context.user().getNickname(),
                context.user().getAvatar().getImageUrl());
    }

    private UserLeagueRankResponse getUserLeagueRankFromScore(Long userId, LeaderboardSeason season) {
        InSeasonParticipantContext context = resolveInSeasonParticipant(userId, season);
        LeaderboardScore score =
                scoreRepository
                        .findByIdSeasonIdAndIdUserId(context.seasonId(), userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);
        long rank =
                scoreRepository.findLeagueRank(
                        context.seasonId(), context.leagueId(), score.getWeeklyXp(), userId);
        return new UserLeagueRankResponse(
                Math.toIntExact(rank),
                score.getWeeklyXp(),
                context.user().getNickname(),
                context.user().getAvatar().getImageUrl());
    }

    private UserGroupRankResponse getUserGroupRankFromScore(Long userId, LeaderboardSeason season) {
        InSeasonParticipantContext context = resolveInSeasonParticipant(userId, season);
        LeaderboardScore score =
                scoreRepository
                        .findByIdSeasonIdAndIdUserId(context.seasonId(), userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);
        long rank =
                scoreRepository.findGroupRank(
                        context.seasonId(), context.groupId(), score.getWeeklyXp(), userId);
        return new UserGroupRankResponse(
                context.groupId(),
                Math.toIntExact(rank),
                score.getWeeklyXp(),
                context.user().getNickname(),
                context.user().getAvatar().getImageUrl());
    }

    private LeaderboardRankPageResponse getGlobalLeaderboardFromScore(
            Long userId, int startRank, int limit, LeaderboardSeason season) {
        validateRange(startRank, limit);
        resolveInSeasonParticipant(userId, season);
        int offset = startRank - 1;
        List<LeaderboardScore> scores =
                scoreRepository.findGlobalSlice(season.getSeasonId(), offset, limit);
        return toGlobalRankPageFromScores(startRank, limit, scores, season.getSeasonId());
    }

    private LeaderboardRankPageResponse getLeagueLeaderboardFromScore(
            Long userId, int startRank, int limit, LeaderboardSeason season) {
        validateRange(startRank, limit);
        InSeasonParticipantContext context = resolveInSeasonParticipant(userId, season);
        int offset = startRank - 1;
        List<LeaderboardScore> scores =
                scoreRepository.findLeagueSlice(context.seasonId(), context.leagueId(), offset, limit);
        long totalCount =
                scoreRepository.countByIdSeasonIdAndLeagueId(context.seasonId(), context.leagueId());
        return toScopedRankPageFromScores(startRank, scores, context.seasonId(), totalCount);
    }

    private LeaderboardGroupPageResponse getGroupLeaderboardFromScore(
            Long userId, int startRank, int limit, LeaderboardSeason season) {
        validateRange(startRank, limit);
        InSeasonParticipantContext context = resolveInSeasonParticipant(userId, season);
        int offset = startRank - 1;
        List<LeaderboardScore> scores =
                scoreRepository.findGroupSlice(context.seasonId(), context.groupId(), offset, limit);
        long totalCount =
                scoreRepository.countByIdSeasonIdAndGroupId(context.seasonId(), context.groupId());
        LeaderboardRankPageResponse page =
                toScopedRankPageFromScores(startRank, scores, context.seasonId(), totalCount);
        return new LeaderboardGroupPageResponse(
                context.seasonId(),
                context.groupId(),
                null,
                page.startRank(),
                page.endRank(),
                page.hasMore(),
                page.ranks());
    }

    private InSeasonParticipantContext resolveInSeasonParticipant(
            Long userId, LeaderboardSeason season) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getLeague() == null) {
            throw new NotLeaderboardParticipantException();
        }
        LeaderboardGroupMember member =
                groupMemberRepository
                        .findFirstBySeasonIdAndUserId(season.getSeasonId(), userId)
                        .orElseThrow(NotLeaderboardParticipantException::new);
        return new InSeasonParticipantContext(
                season.getSeasonId(), user.getLeague().getId(), member.getGroup().getId(), user);
    }

    private SnapshotParticipantContext resolveSnapshotParticipant(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        LeaderboardSeason season =
                findSeasonForRead().orElseThrow(NotLeaderboardParticipantException::new);
        LeaderboardSnapshotBatch snapshotBatch =
                findLatestSnapshotBatch(season).orElseThrow(NotLeaderboardParticipantException::new);
        Long leagueId =
                snapshotRepository
                        .findScopeIdBySnapshotAndUser(
                                snapshotBatch.getId(), LeaderboardScopeType.LEAGUE, userId)
                        .orElseGet(
                                () -> user.getLeague() == null ? null : user.getLeague().getId().longValue());
        Long groupId =
                snapshotRepository
                        .findScopeIdBySnapshotAndUser(snapshotBatch.getId(), LeaderboardScopeType.GROUP, userId)
                        .orElse(null);
        if (leagueId == null || groupId == null) {
            throw new NotLeaderboardParticipantException();
        }

        return new SnapshotParticipantContext(
                season.getSeasonId(), snapshotBatch.getId(), leagueId, groupId, user);
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

    private Optional<UserGlobalRankResponse> getUserGlobalRankFromEndedSeasonRedis(Long userId) {
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
        String key = leaderboardRedisKeyFactory.globalKey(context.seasonId());
        Long rank = leaderboardRedisRepository.reverseRank(key, userId);
        Double score = leaderboardRedisRepository.score(key, userId);
        if (rank == null || score == null) {
            return Optional.empty();
        }
        return Optional.of(
                new UserGlobalRankResponse(
                        rank.intValue() + 1,
                        leaderboardRedisScoreCodec.decodeWeeklyXp(score),
                        context.user().getNickname(),
                        context.user().getAvatar().getImageUrl()));
    }

    private Optional<UserLeagueRankResponse> getUserLeagueRankFromEndedSeasonRedis(Long userId) {
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
        String key =
                leaderboardRedisKeyFactory.leagueKey(context.seasonId(), context.leagueId().intValue());
        Long rank = leaderboardRedisRepository.reverseRank(key, userId);
        Double score = leaderboardRedisRepository.score(key, userId);
        if (rank == null || score == null) {
            return Optional.empty();
        }
        return Optional.of(
                new UserLeagueRankResponse(
                        rank.intValue() + 1,
                        leaderboardRedisScoreCodec.decodeWeeklyXp(score),
                        context.user().getNickname(),
                        context.user().getAvatar().getImageUrl()));
    }

    private Optional<UserGroupRankResponse> getUserGroupRankFromEndedSeasonRedis(Long userId) {
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
        String key = leaderboardRedisKeyFactory.groupKey(context.seasonId(), context.groupId());
        Long rank = leaderboardRedisRepository.reverseRank(key, userId);
        Double score = leaderboardRedisRepository.score(key, userId);
        if (rank == null || score == null) {
            return Optional.empty();
        }
        return Optional.of(
                new UserGroupRankResponse(
                        context.groupId(),
                        rank.intValue() + 1,
                        leaderboardRedisScoreCodec.decodeWeeklyXp(score),
                        context.user().getNickname(),
                        context.user().getAvatar().getImageUrl()));
    }

    private Optional<LeaderboardRankPageResponse> getGlobalLeaderboardFromEndedSeasonRedis(
            Long userId, int startRank, int limit) {
        validateRange(startRank, limit);
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
        String key = leaderboardRedisKeyFactory.globalKey(context.seasonId());
        LeaderboardRankPageResponse page = readRankPageFromRedis(key, key, startRank, limit);
        if (startRank == 1 && page.ranks().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(page);
    }

    private Optional<LeaderboardRankPageResponse> getLeagueLeaderboardFromEndedSeasonRedis(
            Long userId, int startRank, int limit) {
        validateRange(startRank, limit);
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
        String globalKey = leaderboardRedisKeyFactory.globalKey(context.seasonId());
        String leagueKey =
                leaderboardRedisKeyFactory.leagueKey(context.seasonId(), context.leagueId().intValue());
        LeaderboardRankPageResponse page =
                readRankPageFromRedis(leagueKey, globalKey, startRank, limit);
        if (startRank == 1 && page.ranks().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(page);
    }

    private Optional<LeaderboardGroupPageResponse> getGroupLeaderboardFromEndedSeasonRedis(
            Long userId, int startRank, int limit) {
        validateRange(startRank, limit);
        SnapshotParticipantContext context = resolveSnapshotParticipant(userId);
        String globalKey = leaderboardRedisKeyFactory.globalKey(context.seasonId());
        String groupKey = leaderboardRedisKeyFactory.groupKey(context.seasonId(), context.groupId());
        LeaderboardRankPageResponse page = readRankPageFromRedis(groupKey, globalKey, startRank, limit);
        if (startRank == 1 && page.ranks().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                new LeaderboardGroupPageResponse(
                        context.seasonId(),
                        context.groupId(),
                        context.snapshotId(),
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

    private LeaderboardRankPageResponse toGlobalRankPageFromScores(
            int startRank, int limit, List<LeaderboardScore> scores, Integer seasonId) {
        List<LeaderboardRankItem> ranks = new ArrayList<>();
        for (int index = 0; index < scores.size(); index++) {
            LeaderboardScore score = scores.get(index);
            User user = score.getUser();
            ranks.add(
                    new LeaderboardRankItem(
                            startRank + index,
                            user.getId(),
                            user.getAvatar().getImageUrl(),
                            user.getNickname(),
                            score.getWeeklyXp()));
        }
        int endRank = ranks.isEmpty() ? startRank - 1 : startRank + ranks.size() - 1;
        long totalCount = scoreRepository.countByIdSeasonId(seasonId);
        boolean hasMore = endRank < totalCount;
        return new LeaderboardRankPageResponse(startRank, endRank, hasMore, ranks);
    }

    private LeaderboardRankPageResponse toScopedRankPageFromScores(
            int startRank, List<LeaderboardScore> scores, Integer seasonId, long totalCount) {
        List<LeaderboardRankItem> ranks = new ArrayList<>();
        for (LeaderboardScore score : scores) {
            User user = score.getUser();
            long globalRank = scoreRepository.findGlobalRank(seasonId, score.getWeeklyXp(), user.getId());
            ranks.add(
                    new LeaderboardRankItem(
                            Math.toIntExact(globalRank),
                            user.getId(),
                            user.getAvatar().getImageUrl(),
                            user.getNickname(),
                            score.getWeeklyXp()));
        }
        int endRank = ranks.isEmpty() ? startRank - 1 : startRank + ranks.size() - 1;
        boolean hasMore = endRank < totalCount;
        return new LeaderboardRankPageResponse(startRank, endRank, hasMore, ranks);
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(SEOUL).toLocalDate();
    }

    private record SnapshotParticipantContext(
            Integer seasonId, Long snapshotId, Long leagueId, Long groupId, User user) {}

    private record InSeasonParticipantContext(
            Integer seasonId, Integer leagueId, Long groupId, User user) {}

    private record RedisParticipantContext(
            Integer seasonId, Long leagueId, Long groupId, User user) {}
}
