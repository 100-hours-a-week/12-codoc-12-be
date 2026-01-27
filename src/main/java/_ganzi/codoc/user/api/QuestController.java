package _ganzi.codoc.user.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.QuestService;
import _ganzi.codoc.user.service.dto.QuestRewardResponse;
import _ganzi.codoc.user.service.dto.UserQuestListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/quests")
public class QuestController implements QuestApi {

    private final QuestService questService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<UserQuestListResponse>> getUserQuests(
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(ApiResponse.success(questService.getUserQuests(authUser.userId())));
    }

    @Override
    @PostMapping("/{userQuestId}")
    public ResponseEntity<ApiResponse<QuestRewardResponse>> claimQuestReward(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long userQuestId) {
        return ResponseEntity.ok(
                ApiResponse.success(questService.claimReward(authUser.userId(), userQuestId)));
    }
}
