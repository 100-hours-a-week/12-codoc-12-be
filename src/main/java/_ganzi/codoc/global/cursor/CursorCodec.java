package _ganzi.codoc.global.cursor;

public interface CursorCodec {

    String encode(Object payload);

    <T> T decode(String encodedCursor, Class<T> targetType);
}
