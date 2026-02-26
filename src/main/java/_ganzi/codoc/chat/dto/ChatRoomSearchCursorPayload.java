package _ganzi.codoc.chat.dto;

import _ganzi.codoc.global.cursor.ValidatableCursorPayload;
import _ganzi.codoc.global.exception.InvalidCursorFormatException;
import java.time.Instant;

public record ChatRoomSearchCursorPayload(Instant orderedAt, Long roomId)
        implements ValidatableCursorPayload {

    public static ChatRoomSearchCursorPayload firstPage() {
        return new ChatRoomSearchCursorPayload(null, null);
    }

    public static ChatRoomSearchCursorPayload from(ChatRoomListItem item) {
        return new ChatRoomSearchCursorPayload(item.lastMessageAt(), item.roomId());
    }

    public void validateProvidedCursor() {
        if (orderedAt == null || roomId == null || roomId <= 0) {
            throw new InvalidCursorFormatException();
        }
    }
}
