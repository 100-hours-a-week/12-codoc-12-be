package _ganzi.codoc.chatbot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record ChatbotConversationListCondition(
        @NotNull @Positive Long problemId, Long cursor, Integer limit) {

    public ChatbotConversationListCondition {
        if (cursor == null || cursor < 0) cursor = 0L;
        if (limit == null || limit < 1) limit = 20;
        if (limit > 50) limit = 50;
    }
}
