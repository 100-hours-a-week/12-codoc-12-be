package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.RecommendedProblem;
import _ganzi.codoc.problem.enums.ProblemDifficulty;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;

public record RecommendedProblemResponse(ProblemSummary problem) {

    public static RecommendedProblemResponse of(
            RecommendedProblem recommendedProblem, ProblemSolvingStatus status, boolean bookmarked) {
        Problem problem = recommendedProblem.getProblem();
        return new RecommendedProblemResponse(
                new ProblemSummary(
                        problem.getId(),
                        problem.getTitle(),
                        problem.getDifficulty(),
                        status,
                        bookmarked,
                        recommendedProblem.getReasonMsg()));
    }

    public record ProblemSummary(
            Long problemId,
            String title,
            ProblemDifficulty difficulty,
            ProblemSolvingStatus status,
            boolean bookmarked,
            String reason) {}
}
