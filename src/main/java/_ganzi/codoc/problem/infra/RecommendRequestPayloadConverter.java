package _ganzi.codoc.problem.infra;

import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class RecommendRequestPayloadConverter
        implements AttributeConverter<RecommendRequest, String> {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @Override
    public String convertToDatabaseColumn(RecommendRequest attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return JSON_MAPPER.writeValueAsString(attribute);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "Failed to serialize recommend request payload.", exception);
        }
    }

    @Override
    public RecommendRequest convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return JSON_MAPPER.readValue(dbData, RecommendRequest.class);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "Failed to deserialize recommend request payload.", exception);
        }
    }
}
