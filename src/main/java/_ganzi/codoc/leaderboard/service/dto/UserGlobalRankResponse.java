package _ganzi.codoc.leaderboard.service.dto;

public record UserGlobalRankResponse(
        int placeGlobal, int weeklyXp, String nickname, String avatarUrl) {}
