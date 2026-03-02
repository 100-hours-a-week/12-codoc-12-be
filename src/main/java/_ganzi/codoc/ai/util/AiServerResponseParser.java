package _ganzi.codoc.ai.util;

import _ganzi.codoc.ai.dto.AiServerChatbotEvent;
import _ganzi.codoc.global.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Component
public class AiServerResponseParser {

    private final JsonMapper jsonMapper;

    public <T> AiServerChatbotEvent<T> parseChatbotEvent(String data, Class<T> resultType) {
        if (data == null || data.isBlank()) {
            return null;
        }

        JsonNode rootNode = JsonUtils.parseJson(jsonMapper, data);
        if (rootNode == null) {
            return null;
        }

        T result = null;
        JsonNode resultNode = rootNode.get("result");
        if (resultNode != null && !resultNode.isNull()) {
            try {
                result = jsonMapper.convertValue(resultNode, resultType);
            } catch (Exception exception) {
                return null;
            }
        }

        JsonNode codeNode = rootNode.get("code");
        JsonNode messageNode = rootNode.get("message");

        String code = codeNode != null && !codeNode.isNull() ? codeNode.asString() : null;
        String message = messageNode != null && !messageNode.isNull() ? messageNode.asString() : null;

        return new AiServerChatbotEvent<>(code, message, result);
    }
}
