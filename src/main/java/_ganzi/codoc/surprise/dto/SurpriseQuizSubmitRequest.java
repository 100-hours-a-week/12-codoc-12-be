package _ganzi.codoc.surprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SurpriseQuizSubmitRequest(
        @NotNull(message = "choiceNo는 필수입니다.")
                @Min(value = 1, message = "choiceNo는 1 이상이어야 합니다.")
                @Max(value = 4, message = "choiceNo는 4 이하여야 합니다.")
                Integer choiceNo) {}
