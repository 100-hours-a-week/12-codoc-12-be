package _ganzi.codoc.leaderboard.service.dto;

import java.util.List;

public record LeaderboardGroupPageResponse(
        Integer seasonId,
        Long groupId,
        Long snapshotId,
        int startRank,
        int endRank,
        boolean hasMore,
        List<LeaderboardRankItem> ranks) {}
