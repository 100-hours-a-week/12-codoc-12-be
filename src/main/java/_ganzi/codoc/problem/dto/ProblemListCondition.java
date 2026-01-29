package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.enums.ProblemDifficulty;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import java.util.List;
import lombok.Builder;

@Builder
public record ProblemListCondition(
        Long cursor,
        Integer limit,
        String query,
        List<ProblemDifficulty> difficulties,
        List<ProblemSolvingStatus> statuses,
        Boolean bookmarked) {

    public ProblemListCondition {
        if (cursor == null || cursor < 0) cursor = 0L;
        if (limit == null || limit < 1) limit = 20;
        if (limit > 50) limit = 50;
        if (query == null) query = "";
        if (difficulties == null || difficulties.isEmpty())
            difficulties = List.of(ProblemDifficulty.values());
        if (statuses == null || statuses.isEmpty()) statuses = List.of(ProblemSolvingStatus.values());
        if (bookmarked == null) bookmarked = false;
    }
}
