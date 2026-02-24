package _ganzi.codoc.leaderboard.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.leaderboard.service.LeaderboardQueryService;
import _ganzi.codoc.leaderboard.service.dto.UserGlobalRankResponse;
import _ganzi.codoc.leaderboard.service.dto.UserGroupRankResponse;
import _ganzi.codoc.leaderboard.service.dto.UserLeagueRankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/leaderboards")
public class LeaderboardUserController implements LeaderboardUserApi {

    private final LeaderboardQueryService leaderboardQueryService;

    @Override
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<UserGlobalRankResponse>> getUserGlobalRank(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                ApiResponse.success(leaderboardQueryService.getUserGlobalRank(authUser.userId())));
    }

    @Override
    @GetMapping("/league")
    public ResponseEntity<ApiResponse<UserLeagueRankResponse>> getUserLeagueRank(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                ApiResponse.success(leaderboardQueryService.getUserLeagueRank(authUser.userId())));
    }

    @Override
    @GetMapping("/group")
    public ResponseEntity<ApiResponse<UserGroupRankResponse>> getUserGroupRank(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                ApiResponse.success(leaderboardQueryService.getUserGroupRank(authUser.userId())));
    }
}
