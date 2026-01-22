package _ganzi.codoc.global.util;

import _ganzi.codoc.global.dto.CursorPagingResponse;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class CursorPagingUtils {

    private static final int CURSOR_PAGE_INDEX = 0;

    public static <T, C> CursorPagingResponse<T, C> apply(
            List<T> items, int limit, Function<T, C> cursorExtractor) {
        boolean hasNextPage = items.size() > limit;
        List<T> slicedItems = hasNextPage ? items.subList(0, limit) : items;
        C nextCursor =
                hasNextPage && !slicedItems.isEmpty() ? cursorExtractor.apply(slicedItems.getLast()) : null;

        return new CursorPagingResponse<>(slicedItems, nextCursor, hasNextPage);
    }

    public static Pageable createPageable(int limit) {
        int pageSizeForNextPageCheck = limit + 1;
        return PageRequest.of(CURSOR_PAGE_INDEX, pageSizeForNextPageCheck);
    }
}
