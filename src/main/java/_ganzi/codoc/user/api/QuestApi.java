package _ganzi.codoc.user.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.dto.QuestRewardResponse;
import _ganzi.codoc.user.service.dto.UserQuestListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Quest", description = "User quest endpoints")
public interface QuestApi {

    @Operation(summary = "Get user quests")
    ResponseEntity<ApiResponse<UserQuestListResponse>> getUserQuests(AuthUser authUser);

    @Operation(summary = "Claim quest reward")
    ResponseEntity<ApiResponse<QuestRewardResponse>> claimQuestReward(
            AuthUser authUser, Long userQuestId);
}
