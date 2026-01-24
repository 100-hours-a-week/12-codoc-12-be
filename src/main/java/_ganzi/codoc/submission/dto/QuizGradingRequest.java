package _ganzi.codoc.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizGradingRequest(
        @NotNull Integer choiceId, @NotBlank String idempotencyKey, Long attemptId) {}
