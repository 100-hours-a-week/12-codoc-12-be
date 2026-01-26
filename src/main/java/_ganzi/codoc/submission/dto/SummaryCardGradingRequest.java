package _ganzi.codoc.submission.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SummaryCardGradingRequest(@NotNull List<Integer> choiceIds) {}
