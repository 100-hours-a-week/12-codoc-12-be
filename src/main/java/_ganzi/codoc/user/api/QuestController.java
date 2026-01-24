package _ganzi.codoc.user.api;

import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.QuestService;
import _ganzi.codoc.user.service.dto.QuestRewardResponse;
import _ganzi.codoc.user.service.dto.UserQuestListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/quests")
public class QuestController {

    private final QuestService questService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserQuestListResponse>> getUserQuests(
            @RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(questService.getUserQuests(userId)));
    }

    @PostMapping("/{userQuestId}")
    public ResponseEntity<ApiResponse<QuestRewardResponse>> claimQuestReward(
            @RequestHeader("X-USER-ID") Long userId, @PathVariable Long userQuestId) {
        return ResponseEntity.ok(ApiResponse.success(questService.claimReward(userId, userQuestId)));
    }
}
