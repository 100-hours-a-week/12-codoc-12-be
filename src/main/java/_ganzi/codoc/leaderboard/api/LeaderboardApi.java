package _ganzi.codoc.leaderboard.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import _ganzi.codoc.leaderboard.exception.LeaderboardErrorCode;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardGroupPageResponse;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardRankPageResponse;
import _ganzi.codoc.leaderboard.service.dto.LeaderboardSeasonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Leaderboard", description = "Leaderboard read endpoints")
public interface LeaderboardApi {

    @Operation(summary = "Get current season")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
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
            })
    ResponseEntity<ApiResponse<LeaderboardSeasonResponse>> getCurrentSeason(AuthUser authUser);

    @Operation(summary = "Get global leaderboard")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_START_RANK"),
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
            leaderboard = {
                LeaderboardErrorCode.NOT_LEADERBOARD_PARTICIPANT,
                LeaderboardErrorCode.INVALID_START_RANK
            })
    ResponseEntity<ApiResponse<LeaderboardRankPageResponse>> getGlobalLeaderboard(
            AuthUser authUser, int startRank, int limit);

    @Operation(summary = "Get league leaderboard")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_START_RANK"),
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
            leaderboard = {
                LeaderboardErrorCode.NOT_LEADERBOARD_PARTICIPANT,
                LeaderboardErrorCode.INVALID_START_RANK
            })
    ResponseEntity<ApiResponse<LeaderboardRankPageResponse>> getLeagueLeaderboard(
            AuthUser authUser, int startRank, int limit);

    @Operation(summary = "Get group leaderboard")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_START_RANK"),
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
            leaderboard = {
                LeaderboardErrorCode.NOT_LEADERBOARD_PARTICIPANT,
                LeaderboardErrorCode.INVALID_START_RANK
            })
    ResponseEntity<ApiResponse<LeaderboardGroupPageResponse>> getGroupLeaderboard(
            AuthUser authUser, int startRank, int limit);
}
