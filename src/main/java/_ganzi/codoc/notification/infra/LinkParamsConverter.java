package _ganzi.codoc.notification.infra;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class LinkParamsConverter implements AttributeConverter<Map<String, String>, String> {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();
    private static final TypeReference<Map<String, String>> TYPE_REFERENCE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            return JSON_MAPPER.writeValueAsString(attribute);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Failed to serialize link params.", exception);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Map.of();
        }

        try {
            return JSON_MAPPER.readValue(dbData, TYPE_REFERENCE);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Failed to deserialize link params.", exception);
        }
    }
}
