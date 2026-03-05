package _ganzi.codoc.global.cursor;

import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.global.util.CursorPagingUtils;
import _ganzi.codoc.global.util.PageLimitResolver;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DefaultCursorPageFetcher implements CursorPageFetcher {

    private final CursorCodec cursorCodec;

    @Override
    public <P extends ValidatableCursorPayload, T, R> CursorPagingResponse<R, String> fetch(
            String cursor,
            Integer limit,
            Class<P> payloadType,
            Supplier<P> firstPageSupplier,
            BiFunction<P, Pageable, List<T>> queryFunction,
            Function<List<T>, List<R>> itemMapper,
            Function<R, P> nextCursorPayloadMapper) {

        int resolvedLimit = PageLimitResolver.resolve(limit);
        P cursorPayload =
                CursorPayloadConverter.decodeAndValidate(
                        cursorCodec, cursor, payloadType, firstPageSupplier);

        Pageable pageable = CursorPagingUtils.createPageable(resolvedLimit);
        List<T> fetchedItems = queryFunction.apply(cursorPayload, pageable);
        List<R> items = itemMapper.apply(fetchedItems);

        return CursorPagingUtils.apply(
                items, resolvedLimit, item -> cursorCodec.encode(nextCursorPayloadMapper.apply(item)));
    }
}
