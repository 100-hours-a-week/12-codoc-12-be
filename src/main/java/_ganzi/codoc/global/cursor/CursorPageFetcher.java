package _ganzi.codoc.global.cursor;

import _ganzi.codoc.global.dto.CursorPagingResponse;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.data.domain.Pageable;

public interface CursorPageFetcher {

    <P extends ValidatableCursorPayload, T, R> CursorPagingResponse<R, String> fetch(
            String cursor,
            Integer limit,
            Class<P> payloadType,
            Supplier<P> firstPageSupplier,
            BiFunction<P, Pageable, List<T>> queryFunction,
            Function<List<T>, List<R>> itemMapper,
            Function<R, P> nextCursorPayloadMapper);
}
