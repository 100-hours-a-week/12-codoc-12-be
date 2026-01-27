package _ganzi.codoc.user.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.UserStatsService;
import _ganzi.codoc.user.service.dto.UserContributionResponse;
import _ganzi.codoc.user.service.dto.UserStatsResponse;
import _ganzi.codoc.user.service.dto.UserStreakResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserStatsController implements UserStatsApi {

    private final UserStatsService userStatsService;

    @Override
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResponse.success(userStatsService.getUserStats(authUser.userId())));
    }

    @Override
    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<UserStreakResponse>> getUserStreak(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                ApiResponse.success(userStatsService.getUserStreak(authUser.userId())));
    }

    @Override
    @GetMapping("/contribution")
    public ResponseEntity<ApiResponse<UserContributionResponse>> getUserContribution(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("to_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        userStatsService.getUserContribution(authUser.userId(), fromDate, toDate)));
    }
}
