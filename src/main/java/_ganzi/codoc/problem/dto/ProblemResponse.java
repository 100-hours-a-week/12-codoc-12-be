package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.enums.ProblemDifficulty;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import lombok.Builder;

@Builder
public record ProblemResponse(
        Long problemId,
        String title,
        ProblemDifficulty difficulty,
        String content,
        ProblemSolvingStatus status,
        boolean bookmarked) {

    public static ProblemResponse of(
            Problem problem, ProblemSolvingStatus status, boolean bookmarked) {

        return ProblemResponse.builder()
                .problemId(problem.getId())
                .title(problem.getTitle())
                .difficulty(problem.getDifficulty())
                .content(problem.getContent())
                .status(status)
                .bookmarked(bookmarked)
                .build();
    }
}
