package _ganzi.codoc.global.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public final class JsonUtils {

    private JsonUtils() {}

    public static JsonNode parseJson(JsonMapper jsonMapper, String data) {
        try {
            return jsonMapper.readTree(data);
        } catch (Exception exception) {
            return null;
        }
    }

    public static <T> T parseJson(JsonMapper jsonMapper, String data, Class<T> targetType) {
        try {
            return jsonMapper.readValue(data, targetType);
        } catch (Exception exception) {
            return null;
        }
    }
}
