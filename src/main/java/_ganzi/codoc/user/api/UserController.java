package _ganzi.codoc.user.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(authUser.userId())));
    }

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

    @PatchMapping("/init-survey")
    public ResponseEntity<Void> saveInitSurvey(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserInitSurveyRequest request) {
        userService.completeOnboarding(authUser.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/daily-goal")
    public ResponseEntity<ApiResponse<UserDailyGoalResponse>> updateDailyGoal(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody UserDailyGoalRequest request) {
        userService.updateDailyGoal(authUser.userId(), request.dailyGoal());
        return ResponseEntity.ok(ApiResponse.success(new UserDailyGoalResponse(request.dailyGoal())));
    }
}
