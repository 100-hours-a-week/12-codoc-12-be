package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.enums.ProblemDifficulty;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;

public record ProblemSearchParam(
        Long userId,
        Long cursor,
        String query,
        List<ProblemDifficulty> difficulties,
        List<ProblemSolvingStatus> statuses,
        ProblemSolvingStatus defaultStatus,
        boolean bookmarked,
        Pageable pageable) {}
