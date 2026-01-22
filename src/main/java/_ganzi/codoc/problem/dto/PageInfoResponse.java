package _ganzi.codoc.problem.dto;

import lombok.Builder;

@Builder
public record PageInfoResponse(Long nextCursor, boolean hasNextPage) {

    public static PageInfoResponse of(Long nextCursor, boolean hasNextPage) {
        return PageInfoResponse.builder().nextCursor(nextCursor).hasNextPage(hasNextPage).build();
    }
}
