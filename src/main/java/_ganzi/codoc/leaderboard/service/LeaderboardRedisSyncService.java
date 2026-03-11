package _ganzi.codoc.leaderboard.service;

import _ganzi.codoc.leaderboard.config.LeaderboardRedisProperties;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisKeyFactory;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisRepository;
import _ganzi.codoc.leaderboard.infra.redis.LeaderboardRedisScoreCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardRedisSyncService {

    private final LeaderboardRedisProperties properties;
    private final LeaderboardRedisKeyFactory keyFactory;
    private final LeaderboardRedisScoreCodec scoreCodec;
    private final LeaderboardRedisRepository redisRepository;

    public void syncScoreAfterCommit(
            Integer seasonId, Integer leagueId, Long groupId, Long userId, int weeklyXp) {
        if (!properties.writeEnabled()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            syncScore(seasonId, leagueId, groupId, userId, weeklyXp);
                        }
                    });
            return;
        }
        syncScore(seasonId, leagueId, groupId, userId, weeklyXp);
    }

    public void syncScore(
            Integer seasonId, Integer leagueId, Long groupId, Long userId, int weeklyXp) {
        if (!properties.writeEnabled()) {
            return;
        }
        double encodedScore = scoreCodec.encode(weeklyXp, userId);
        try {
            redisRepository.upsertScore(keyFactory.globalKey(seasonId), userId, encodedScore);
            redisRepository.upsertScore(keyFactory.leagueKey(seasonId, leagueId), userId, encodedScore);
            if (groupId != null) {
                redisRepository.upsertScore(keyFactory.groupKey(seasonId, groupId), userId, encodedScore);
            }
        } catch (Exception exception) {
            log.warn(
                    "leaderboard redis sync failed. seasonId={}, leagueId={}, groupId={}," + " userId={}",
                    seasonId,
                    leagueId,
                    groupId,
                    userId,
                    exception);
        }
    }
}
