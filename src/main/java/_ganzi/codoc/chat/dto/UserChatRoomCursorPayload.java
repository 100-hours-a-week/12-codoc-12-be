package _ganzi.codoc.chat.dto;

import _ganzi.codoc.global.cursor.ValidatableCursorPayload;
import _ganzi.codoc.global.exception.InvalidCursorFormatException;
import java.time.Instant;

public record UserChatRoomCursorPayload(Instant orderedAt, Long roomId)
        implements ValidatableCursorPayload {

    public static UserChatRoomCursorPayload firstPage() {
        return new UserChatRoomCursorPayload(null, null);
    }

    public static UserChatRoomCursorPayload from(UserChatRoomListItem item) {
        return new UserChatRoomCursorPayload(item.lastMessageAt(), item.roomId());
    }

    public void validateProvidedCursor() {
        if (orderedAt == null || roomId == null || roomId <= 0) {
            throw new InvalidCursorFormatException();
        }
    }
}
