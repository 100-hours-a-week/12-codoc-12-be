package _ganzi.codoc.leaderboard.service.dto;

public record UserLeagueInfoResponse(
        Integer seasonId,
        Long snapshotId,
        Integer leagueId,
        String leagueName,
        String logoUrl,
        Long groupId) {}
