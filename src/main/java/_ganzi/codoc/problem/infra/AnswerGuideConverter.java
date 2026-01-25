package _ganzi.codoc.problem.infra;

import _ganzi.codoc.problem.domain.AnswerGuideItem;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Converter
public class AnswerGuideConverter implements AttributeConverter<List<AnswerGuideItem>, String> {

    private final JsonMapper jsonMapper;

    @Override
    public String convertToDatabaseColumn(List<AnswerGuideItem> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return jsonMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize answer_guides", e);
        }
    }

    @Override
    public List<AnswerGuideItem> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return jsonMapper.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize answer_guides", e);
        }
    }
}
