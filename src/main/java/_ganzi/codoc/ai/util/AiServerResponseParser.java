package _ganzi.codoc.ai.util;

import _ganzi.codoc.ai.dto.AiServerChatbotFinalEvent;
import _ganzi.codoc.ai.dto.AiServerErrorEvent;
import _ganzi.codoc.global.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Component
public class AiServerResponseParser {

    private final JsonMapper jsonMapper;

    public AiServerChatbotFinalEvent parseChatbotFinalEvent(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        return JsonUtils.parseJson(jsonMapper, data, AiServerChatbotFinalEvent.class);
    }

    public AiServerErrorEvent parseErrorEvent(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        return JsonUtils.parseJson(jsonMapper, data, AiServerErrorEvent.class);
    }
}
