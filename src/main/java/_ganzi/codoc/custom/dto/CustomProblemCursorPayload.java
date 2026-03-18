package _ganzi.codoc.custom.dto;

import _ganzi.codoc.global.cursor.ValidatableCursorPayload;
import _ganzi.codoc.global.exception.InvalidCursorFormatException;
import java.time.Instant;

public record CustomProblemCursorPayload(Instant createdAt, Long customProblemId)
        implements ValidatableCursorPayload {

    public static CustomProblemCursorPayload firstPage() {
        return new CustomProblemCursorPayload(null, null);
    }

    public static CustomProblemCursorPayload from(CustomProblemListItem item) {
        return new CustomProblemCursorPayload(item.createdAt(), item.customProblemId());
    }

    @Override
    public void validateProvidedCursor() {
        if (createdAt == null || customProblemId == null || customProblemId <= 0) {
            throw new InvalidCursorFormatException();
        }
    }
}
