package _ganzi.codoc.user.api;

import _ganzi.codoc.analysis.service.AnalysisReportService;
import _ganzi.codoc.analysis.service.dto.AnalysisReportDetailResponse;
import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.leaderboard.service.LeaderboardQueryService;
import _ganzi.codoc.leaderboard.service.dto.UserLeagueInfoResponse;
import _ganzi.codoc.user.api.dto.UserDailyGoalRequest;
import _ganzi.codoc.user.api.dto.UserInitSurveyRequest;
import _ganzi.codoc.user.api.dto.UserProfileUpdateRequest;
import _ganzi.codoc.user.service.UserService;
import _ganzi.codoc.user.service.dto.UserDailyGoalResponse;
import _ganzi.codoc.user.service.dto.UserProfileResponse;
import _ganzi.codoc.user.service.dto.UserProfileUpdateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController implements UserApi {

    private final UserService userService;
    private final LeaderboardQueryService leaderboardQueryService;
    private final AnalysisReportService analysisReportService;

    @Override
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(authUser.userId())));
    }

    @Override
    @GetMapping("/league")
    public ResponseEntity<ApiResponse<UserLeagueInfoResponse>> getUserLeague(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                ApiResponse.success(leaderboardQueryService.getUserLeagueInfo(authUser.userId())));
    }

    @Override
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileUpdateResponse>> updateProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        userService.updateProfile(authUser.userId(), request.nickname(), request.avatarId());
        UserProfileResponse profile = userService.getUserProfile(authUser.userId());
        return ResponseEntity.ok(
                ApiResponse.success(
                        new UserProfileUpdateResponse(profile.nickname(), profile.avatarImageUrl())));
    }

    @Override
    @PatchMapping("/init-survey")
    public ResponseEntity<Void> saveInitSurvey(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserInitSurveyRequest request) {
        userService.completeOnboarding(authUser.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/daily-goal")
    public ResponseEntity<ApiResponse<UserDailyGoalResponse>> updateDailyGoal(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserDailyGoalRequest request) {
        userService.updateDailyGoal(authUser.userId(), request.dailyGoal());
        return ResponseEntity.ok(ApiResponse.success(new UserDailyGoalResponse(request.dailyGoal())));
    }

    @GetMapping("/daily-goal")
    public ResponseEntity<ApiResponse<UserDailyGoalResponse>> getDailyGoal(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getDailyGoal(authUser.userId())));
    }

    @Override
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<AnalysisReportDetailResponse>> getAnalysisReport(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(
                ApiResponse.success(analysisReportService.getLatestReport(authUser.userId())));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AuthUser authUser) {
        userService.deleteUser(authUser.userId());
        return ResponseEntity.noContent().build();
    }
}
