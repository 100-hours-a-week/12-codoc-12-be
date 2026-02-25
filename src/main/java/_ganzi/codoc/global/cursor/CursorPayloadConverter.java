package _ganzi.codoc.global.cursor;

import java.util.function.Supplier;

public final class CursorPayloadConverter {

    private CursorPayloadConverter() {}

    public static <T extends ValidatableCursorPayload> T decodeAndValidate(
            CursorCodec cursorCodec,
            String encodedCursor,
            Class<T> targetType,
            Supplier<T> firstPageSupplier) {

        if (encodedCursor == null) {
            return firstPageSupplier.get();
        }

        T cursorPayload = cursorCodec.decode(encodedCursor, targetType);
        cursorPayload.validateProvidedCursor();
        return cursorPayload;
    }
}
