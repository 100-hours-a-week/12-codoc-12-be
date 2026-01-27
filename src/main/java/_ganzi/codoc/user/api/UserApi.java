package _ganzi.codoc.user.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.api.dto.UserDailyGoalRequest;
import _ganzi.codoc.user.api.dto.UserInitSurveyRequest;
import _ganzi.codoc.user.api.dto.UserProfileUpdateRequest;
import _ganzi.codoc.user.service.dto.UserDailyGoalResponse;
import _ganzi.codoc.user.service.dto.UserProfileResponse;
import _ganzi.codoc.user.service.dto.UserProfileUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User", description = "User profile endpoints")
public interface UserApi {

    @Operation(summary = "Get user profile")
    ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(AuthUser authUser);

    @Operation(summary = "Update user profile")
    ResponseEntity<ApiResponse<UserProfileUpdateResponse>> updateProfile(
            AuthUser authUser, UserProfileUpdateRequest request);

    @Operation(summary = "Complete onboarding survey")
    ResponseEntity<Void> saveInitSurvey(AuthUser authUser, UserInitSurveyRequest request);

    @Operation(summary = "Update daily goal")
    ResponseEntity<ApiResponse<UserDailyGoalResponse>> updateDailyGoal(
            AuthUser authUser, UserDailyGoalRequest request);

    @Operation(summary = "Delete user")
    ResponseEntity<Void> deleteUser(AuthUser authUser);
}
