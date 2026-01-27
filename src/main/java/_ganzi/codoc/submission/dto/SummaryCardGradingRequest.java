package _ganzi.codoc.submission.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SummaryCardGradingRequest(
        @NotNull Long problemId, @NotNull List<Integer> choiceIds) {}
