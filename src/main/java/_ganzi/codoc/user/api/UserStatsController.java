package _ganzi.codoc.user.api;

import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.UserStatsService;
import _ganzi.codoc.user.service.dto.UserContributionResponse;
import _ganzi.codoc.user.service.dto.UserStatsResponse;
import _ganzi.codoc.user.service.dto.UserStreakResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserStatsController {

    private final UserStatsService userStatsService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(
            @RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userStatsService.getUserStats(userId)));
    }

    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<UserStreakResponse>> getUserStreak(
            @RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userStatsService.getUserStreak(userId)));
    }

    @GetMapping("/contribution")
    public ResponseEntity<ApiResponse<UserContributionResponse>> getUserContribution(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam("from_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("to_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(
                ApiResponse.success(userStatsService.getUserContribution(userId, fromDate, toDate)));
    }
}
