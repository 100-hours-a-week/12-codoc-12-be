package _ganzi.codoc.problem.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ProblemListResponse(List<ProblemListItem> problems, PageInfoResponse pageInfo) {

    public static ProblemListResponse of(
            List<ProblemListItem> items, Long nextCursor, boolean hasNextPage) {

        return ProblemListResponse.builder()
                .problems(items)
                .pageInfo(PageInfoResponse.of(nextCursor, hasNextPage))
                .build();
    }
}
