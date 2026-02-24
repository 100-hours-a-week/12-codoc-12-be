package _ganzi.codoc.leaderboard.service.dto;

public record UserGroupRankResponse(
        Long groupId, int placeGroup, int weeklyXp, String nickname, String avatarUrl) {}
