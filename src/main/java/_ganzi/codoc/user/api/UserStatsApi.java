package _ganzi.codoc.user.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.dto.UserContributionResponse;
import _ganzi.codoc.user.service.dto.UserStatsResponse;
import _ganzi.codoc.user.service.dto.UserStreakResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Stats", description = "User statistics endpoints")
public interface UserStatsApi {

    @Operation(summary = "Get user stats")
    ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(AuthUser authUser);

    @Operation(summary = "Get user streak")
    ResponseEntity<ApiResponse<UserStreakResponse>> getUserStreak(AuthUser authUser);

    @Operation(summary = "Get user contribution")
    ResponseEntity<ApiResponse<UserContributionResponse>> getUserContribution(
            AuthUser authUser, LocalDate fromDate, LocalDate toDate);
}
