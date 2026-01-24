package _ganzi.codoc.user.service.dto;

import _ganzi.codoc.user.enums.QuestStatus;
import _ganzi.codoc.user.enums.QuestType;
import java.util.List;

public record UserQuestListResponse(List<UserQuestSummary> quests) {

    public record UserQuestSummary(
            Long userQuestId, String title, int reward, QuestType type, QuestStatus status) {}
}
