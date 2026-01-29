package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.enums.ProblemDifficulty;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import lombok.Builder;

@Builder
public record ProblemListItem(
        Long problemId,
        String title,
        ProblemDifficulty difficulty,
        ProblemSolvingStatus status,
        boolean bookmarked) {}
