package _ganzi.codoc.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatbotMessageSendRequest(@NotNull Long problemId, @NotBlank String message) {}
