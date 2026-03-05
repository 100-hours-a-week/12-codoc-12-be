package _ganzi.codoc.chat.dto;

import _ganzi.codoc.global.cursor.ValidatableCursorPayload;
import _ganzi.codoc.global.exception.InvalidCursorFormatException;
import java.time.Instant;

public record ChatRoomCursorPayload(Instant orderedAt, Long roomId)
        implements ValidatableCursorPayload {

    public static ChatRoomCursorPayload firstPage() {
        return new ChatRoomCursorPayload(null, null);
    }

    public static ChatRoomCursorPayload from(ChatRoomListItem item) {
        return new ChatRoomCursorPayload(item.lastMessageAt(), item.roomId());
    }

    public void validateProvidedCursor() {
        if (orderedAt == null || roomId == null || roomId <= 0) {
            throw new InvalidCursorFormatException();
        }
    }
}
