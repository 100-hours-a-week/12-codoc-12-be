package _ganzi.codoc.leaderboard.service.dto;

import java.time.LocalDate;

public record LeaderboardSeasonResponse(Integer seasonId, LocalDate startsAt, LocalDate endsAt) {}
