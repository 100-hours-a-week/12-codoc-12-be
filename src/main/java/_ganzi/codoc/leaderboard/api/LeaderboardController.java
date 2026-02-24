package _ganzi.codoc.leaderboard.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.leaderboard.service.LeaderboardQueryService;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardGroupPageResponse;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardRankPageResponse;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardSeasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/leaderboards")
public class LeaderboardController implements LeaderboardApi {

    private final LeaderboardQueryService leaderboardQueryService;

    @Override
    @GetMapping("/season")
    public ResponseEntity<ApiResponse<LeaderboardSeasonResponse>> getCurrentSeason(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResponse.success(leaderboardQueryService.getCurrentSeason()));
    }

    @Override
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<LeaderboardRankPageResponse>> getGlobalLeaderboard(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam int startRank,
            @RequestParam int limit) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        leaderboardQueryService.getGlobalLeaderboard(authUser.userId(), startRank, limit)));
    }

    @Override
    @GetMapping("/league")
    public ResponseEntity<ApiResponse<LeaderboardRankPageResponse>> getLeagueLeaderboard(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam int startRank,
            @RequestParam int limit) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        leaderboardQueryService.getLeagueLeaderboard(authUser.userId(), startRank, limit)));
    }

    @Override
    @GetMapping("/group")
    public ResponseEntity<ApiResponse<LeaderboardGroupPageResponse>> getGroupLeaderboard(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam int startRank,
            @RequestParam int limit) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        leaderboardQueryService.getGroupLeaderboard(authUser.userId(), startRank, limit)));
    }
}
