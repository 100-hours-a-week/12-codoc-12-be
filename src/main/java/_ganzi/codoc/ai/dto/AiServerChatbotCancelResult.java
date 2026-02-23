package _ganzi.codoc.ai.dto;

import _ganzi.codoc.ai.enums.ChatbotStatus;

public record AiServerChatbotCancelResult(Long runId, ChatbotStatus status, String message) {}
