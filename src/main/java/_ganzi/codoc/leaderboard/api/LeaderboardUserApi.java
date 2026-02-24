package _ganzi.codoc.leaderboard.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import _ganzi.codoc.leaderboard.exception.LeaderboardErrorCode;
import _ganzi.codoc.leaderboard.service.dto.UserGlobalRankResponse;
import _ganzi.codoc.leaderboard.service.dto.UserGroupRankResponse;
import _ganzi.codoc.leaderboard.service.dto.UserLeagueRankResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Leaderboard", description = "User leaderboard endpoints")
public interface LeaderboardUserApi {

    @Operation(summary = "Get user global rank")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN, NOT_LEADERBOARD_PARTICIPANT"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            leaderboard = {LeaderboardErrorCode.NOT_LEADERBOARD_PARTICIPANT})
    ResponseEntity<ApiResponse<UserGlobalRankResponse>> getUserGlobalRank(AuthUser authUser);

    @Operation(summary = "Get user league rank")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN, NOT_LEADERBOARD_PARTICIPANT"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            leaderboard = {LeaderboardErrorCode.NOT_LEADERBOARD_PARTICIPANT})
    ResponseEntity<ApiResponse<UserLeagueRankResponse>> getUserLeagueRank(AuthUser authUser);

    @Operation(summary = "Get user group rank")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN, NOT_LEADERBOARD_PARTICIPANT"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            leaderboard = {LeaderboardErrorCode.NOT_LEADERBOARD_PARTICIPANT})
    ResponseEntity<ApiResponse<UserGroupRankResponse>> getUserGroupRank(AuthUser authUser);
}
