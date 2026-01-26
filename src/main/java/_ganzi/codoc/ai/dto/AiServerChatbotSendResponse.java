package _ganzi.codoc.ai.dto;

import _ganzi.codoc.ai.enums.ChatbotStatus;

public record AiServerChatbotSendResponse(Long runId, ChatbotStatus status) {}
