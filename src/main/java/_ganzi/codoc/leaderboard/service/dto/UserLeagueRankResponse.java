package _ganzi.codoc.leaderboard.service.dto;

public record UserLeagueRankResponse(
        int placeLeague, int weeklyXp, String nickname, String avatarUrl) {}
