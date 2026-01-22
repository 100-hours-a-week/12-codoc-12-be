package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.enums.ProblemLevel;
import _ganzi.codoc.problem.enums.ProblemSolvingStatus;
import lombok.Builder;

@Builder
public record ProblemListItem(
        Long problemId,
        String title,
        ProblemLevel level,
        ProblemSolvingStatus status,
        boolean bookmarked) {}
