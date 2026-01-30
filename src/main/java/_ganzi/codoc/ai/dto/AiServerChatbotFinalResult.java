package _ganzi.codoc.ai.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiServerChatbotFinalResult(
        String status,
        String aiMessage,
        String paragraphType,
        Boolean isCorrect,
        String currentAnswer) {}
