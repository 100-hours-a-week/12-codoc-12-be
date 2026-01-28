package _ganzi.codoc.user.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import _ganzi.codoc.user.exception.QuestErrorCode;
import _ganzi.codoc.user.exception.UserErrorCode;
import _ganzi.codoc.user.service.dto.QuestRewardResponse;
import _ganzi.codoc.user.service.dto.UserQuestListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Quest", description = "User quest endpoints")
public interface QuestApi {

    @Operation(summary = "Get user quests")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "USER_NOT_FOUND"),
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
            user = {UserErrorCode.USER_NOT_FOUND})
    ResponseEntity<ApiResponse<UserQuestListResponse>> getUserQuests(AuthUser authUser);

    @Operation(summary = "Claim quest reward")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_INPUT"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "QUEST_NOT_FOUND, USER_NOT_FOUND"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "QUEST_IN_PROGRESS, QUEST_ALREADY_CLAIMED, QUEST_EXPIRED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.INVALID_INPUT,
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            quest = {
                QuestErrorCode.QUEST_NOT_FOUND,
                QuestErrorCode.QUEST_IN_PROGRESS,
                QuestErrorCode.QUEST_ALREADY_CLAIMED,
                QuestErrorCode.QUEST_EXPIRED
            },
            user = {UserErrorCode.USER_NOT_FOUND})
    ResponseEntity<ApiResponse<QuestRewardResponse>> claimQuestReward(
            AuthUser authUser, Long userQuestId);
}
