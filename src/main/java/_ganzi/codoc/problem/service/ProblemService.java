package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.dto.ProblemListCondition;
import _ganzi.codoc.problem.dto.ProblemListItem;
import _ganzi.codoc.problem.dto.ProblemListResponse;
import _ganzi.codoc.problem.enums.ProblemLevel;
import _ganzi.codoc.problem.enums.ProblemSolvingStatus;
import _ganzi.codoc.problem.repository.ProblemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemService {

    private static final int CURSOR_PAGE_INDEX = 0;

    private final ProblemRepository problemRepository;

    public ProblemListResponse getProblemList(Long userId, ProblemListCondition condition) {

        int limit = condition.limit();
        List<ProblemLevel> levels = normalizeLevels(condition.levels());
        List<ProblemSolvingStatus> statuses = normalizeStatuses(condition.statuses());

        Pageable pageable = PageRequest.of(CURSOR_PAGE_INDEX, limit + 1);
        List<ProblemListItem> items =
                problemRepository.findProblemList(
                        userId,
                        condition.cursor(),
                        levels,
                        statuses,
                        ProblemSolvingStatus.NOT_ATTEMPTED,
                        condition.bookmarked(),
                        pageable);

        boolean hasNextPage = items.size() > limit;
        List<ProblemListItem> slicedItems = hasNextPage ? items.subList(0, limit) : items;
        Long nextCursor =
                hasNextPage && !slicedItems.isEmpty() ? slicedItems.getLast().problemId() : null;

        return ProblemListResponse.of(slicedItems, nextCursor, hasNextPage);
    }

    private List<ProblemSolvingStatus> normalizeStatuses(List<ProblemSolvingStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of(ProblemSolvingStatus.values());
        }
        return statuses;
    }

    private List<ProblemLevel> normalizeLevels(List<ProblemLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return List.of(ProblemLevel.values());
        }

        return levels;
    }
}
