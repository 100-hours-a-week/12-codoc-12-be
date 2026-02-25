package _ganzi.codoc.global.cursor;

import _ganzi.codoc.global.exception.InvalidCursorFormatException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Component
public class Base64JsonCursorCodec implements CursorCodec {

    private final JsonMapper jsonMapper;

    @Override
    public String encode(Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Cursor payload must not be null");
        }

        try {
            byte[] bytes = jsonMapper.writeValueAsBytes(payload);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to encode cursor", exception);
        }
    }

    @Override
    public <T> T decode(String encodedCursor, Class<T> targetType) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            throw new InvalidCursorFormatException();
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encodedCursor);
            return jsonMapper.readValue(decoded, targetType);
        } catch (Exception exception) {
            throw new InvalidCursorFormatException(exception);
        }
    }
}
