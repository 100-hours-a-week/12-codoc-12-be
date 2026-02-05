package _ganzi.codoc.user.service.requirements;

import _ganzi.codoc.user.domain.UserQuest;
import tools.jackson.databind.JsonNode;

public interface QuestRequirementEvaluator {

    String key();

    boolean isSatisfied(UserQuest userQuest, JsonNode value);
}
