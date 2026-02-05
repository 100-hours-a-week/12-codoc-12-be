package _ganzi.codoc.user.service.requirements;

import _ganzi.codoc.user.domain.UserQuest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class QuestRequirementRegistry {

    private final JsonMapper jsonMapper;
    private final Map<String, QuestRequirementEvaluator> evaluators;

    public QuestRequirementRegistry(
            JsonMapper jsonMapper, List<QuestRequirementEvaluator> evaluatorList) {
        this.jsonMapper = jsonMapper;
        Map<String, QuestRequirementEvaluator> map = new HashMap<>();
        for (QuestRequirementEvaluator evaluator : evaluatorList) {
            QuestRequirementEvaluator existing = map.putIfAbsent(evaluator.key(), evaluator);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate quest requirement evaluator key: " + evaluator.key());
            }
        }
        this.evaluators = Map.copyOf(map);
    }

    public boolean isSatisfied(UserQuest userQuest) {
        String requirementsJson = userQuest.getQuest().getRequirements();
        if (requirementsJson == null || requirementsJson.isBlank()) {
            return true;
        }
        JsonNode requirements = parseRequirements(requirementsJson);
        if (requirements == null || !requirements.isObject()) {
            return false;
        }
        for (Map.Entry<String, QuestRequirementEvaluator> entry : evaluators.entrySet()) {
            String key = entry.getKey();
            if (!requirements.has(key)) {
                continue;
            }
            if (!entry.getValue().isSatisfied(userQuest, requirements.get(key))) {
                return false;
            }
        }
        return true;
    }

    private JsonNode parseRequirements(String requirementsJson) {
        try {
            return jsonMapper.readTree(requirementsJson);
        } catch (Exception exception) {
            return null;
        }
    }
}
