package _ganzi.codoc.chat.dto;

import _ganzi.codoc.chat.domain.ChatMessage;
import _ganzi.codoc.chat.enums.ChatMessageType;
import java.time.Instant;

public record ChatMessageBroadcast(
        Long messageId, Long senderId, ChatMessageType type, String content, Instant createdAt) {

    public static ChatMessageBroadcast from(ChatMessage message) {
        return new ChatMessageBroadcast(
                message.getId(),
                message.getSenderId(),
                message.getType(),
                message.getContent(),
                message.getCreatedAt());
    }
}
