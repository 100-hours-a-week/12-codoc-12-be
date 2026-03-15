package _ganzi.codoc.chat.dto;

public record ChatReadAckBroadcast(
        Long userId, long previousLastReadMessageId, long lastReadMessageId) {}
