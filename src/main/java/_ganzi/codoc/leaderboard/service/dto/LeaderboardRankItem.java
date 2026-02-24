package _ganzi.codoc.leaderboard.service.dto;

public record LeaderboardRankItem(
        int placeGlobal, Long userId, String avatarUrl, String nickname, int weeklyXp) {}
