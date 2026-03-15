package _ganzi.codoc.chat.event;

public record ChatReadAckCommittedEvent(
        Long roomId, Long userId, long previousLastReadMessageId, long lastReadMessageId) {}
