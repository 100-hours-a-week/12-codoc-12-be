package _ganzi.codoc.leaderboard.service.dto;

import java.util.List;

public record LeaderboardRankPageResponse(
        int startRank, int endRank, boolean hasMore, List<LeaderboardRankItem> ranks) {}
