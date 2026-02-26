package _ganzi.codoc.chat.dto;

import _ganzi.codoc.global.cursor.ValidatableCursorPayload;
import _ganzi.codoc.global.exception.InvalidCursorFormatException;

public record ChatMessageCursorPayload(Long messageId) implements ValidatableCursorPayload {

    public static ChatMessageCursorPayload firstPage() {
        return new ChatMessageCursorPayload(null);
    }

    public static ChatMessageCursorPayload from(ChatMessageListItem item) {
        return new ChatMessageCursorPayload(item.messageId());
    }

    public void validateProvidedCursor() {
        if (messageId == null || messageId <= 0) {
            throw new InvalidCursorFormatException();
        }
    }
}
