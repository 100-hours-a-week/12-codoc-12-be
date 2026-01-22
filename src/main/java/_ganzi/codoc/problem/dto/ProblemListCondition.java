package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.enums.ProblemLevel;
import _ganzi.codoc.problem.enums.ProblemSolvingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Builder;

@Builder
public record ProblemListCondition(
        @Positive Long cursor,
        @Min(1) @Max(50) int limit,
        List<ProblemLevel> levels,
        List<ProblemSolvingStatus> statuses,
        boolean bookmarked) {}
