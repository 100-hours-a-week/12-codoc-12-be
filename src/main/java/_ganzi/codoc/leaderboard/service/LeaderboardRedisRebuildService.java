package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.config.LeaderboardRedisProperties;
import _ganzi.codoc.leaderboard.domain.LeaderboardScore;
import _ganzi.codoc.leaderboard.domain.LeaderboardSeason;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisKeyFactory;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisRepository;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisScoreCodec;
import _ganzi.codoc.leaderboard.repository.LeaderboardScoreRepository;
import _ganzi.codoc.leaderboard.repository.LeaderboardSeasonRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardRedisRebuildService {

    private final LeaderboardRedisProperties properties;
    private final LeaderboardSeasonRepository seasonRepository;
    private final LeaderboardScoreRepository scoreRepository;
    private final LeaderboardRedisKeyFactory keyFactory;
    private final LeaderboardRedisRepository redisRepository;
    private final LeaderboardRedisScoreCodec scoreCodec;

    public boolean rebuildCurrentSeasonIfMissing() {
        Optional<LeaderboardSeason> currentSeason = findCurrentSeason();
        if (currentSeason.isEmpty()) {
            return false;
        }
        Integer seasonId = currentSeason.get().getSeasonId();
        String globalKey = keyFactory.globalKey(seasonId);
        if (Boolean.TRUE.equals(redisRepository.hasKey(globalKey))) {
            return false;
        }
        rebuildSeason(seasonId);
        return true;
    }

    public void rebuildSeason(Integer seasonId) {
        if (!properties.writeEnabled()) {
            log.info("skip leaderboard redis rebuild because write-enabled=false");
            return;
        }

        int batchSize = resolveBatchSize();
        Set<Integer> leagueIds = new HashSet<>();
        Set<Long> groupIds = new HashSet<>();

        scanScopeIds(seasonId, batchSize, leagueIds, groupIds);
        clearSeasonKeys(seasonId, leagueIds, groupIds);
        upsertScores(seasonId, batchSize);

        log.info(
                "leaderboard redis rebuild completed. seasonId={}, leagues={}, groups={}",
                seasonId,
                leagueIds.size(),
                groupIds.size());
    }

    private void scanScopeIds(
            Integer seasonId, int batchSize, Set<Integer> leagueIds, Set<Long> groupIds) {
        for (int offset = 0; ; offset += batchSize) {
            List<LeaderboardScore> batch = scoreRepository.findGlobalSlice(seasonId, offset, batchSize);
            if (batch.isEmpty()) {
                return;
            }
            for (LeaderboardScore score : batch) {
                leagueIds.add(score.getLeague().getId());
                if (score.getGroupId() != null) {
                    groupIds.add(score.getGroupId());
                }
            }
            if (batch.size() < batchSize) {
                return;
            }
        }
    }

    private void clearSeasonKeys(Integer seasonId, Set<Integer> leagueIds, Set<Long> groupIds) {
        List<String> keys = new ArrayList<>();
        keys.add(keyFactory.globalKey(seasonId));
        for (Integer leagueId : leagueIds) {
            keys.add(keyFactory.leagueKey(seasonId, leagueId));
        }
        for (Long groupId : groupIds) {
            keys.add(keyFactory.groupKey(seasonId, groupId));
        }
        redisRepository.deleteKeys(keys);
    }

    private void upsertScores(Integer seasonId, int batchSize) {
        for (int offset = 0; ; offset += batchSize) {
            List<LeaderboardScore> batch = scoreRepository.findGlobalSlice(seasonId, offset, batchSize);
            if (batch.isEmpty()) {
                return;
            }
            for (LeaderboardScore score : batch) {
                long userId = score.getUser().getId();
                double encoded = scoreCodec.encode(score.getWeeklyXp(), userId);
                redisRepository.upsertScore(keyFactory.globalKey(seasonId), userId, encoded);
                redisRepository.upsertScore(
                        keyFactory.leagueKey(seasonId, score.getLeague().getId()), userId, encoded);
                if (score.getGroupId() != null) {
                    redisRepository.upsertScore(
                            keyFactory.groupKey(seasonId, score.getGroupId()), userId, encoded);
                }
            }
            if (batch.size() < batchSize) {
                return;
            }
        }
    }

    private int resolveBatchSize() {
        return properties.rebuildBatchSize() <= 0 ? 500 : properties.rebuildBatchSize();
    }

    private Optional<LeaderboardSeason> findCurrentSeason() {
        Instant now = Instant.now();
        return seasonRepository.findFirstByStartsAtLessThanEqualAndEndsAtAfterOrderByStartsAtDesc(
                now, now);
    }
}
