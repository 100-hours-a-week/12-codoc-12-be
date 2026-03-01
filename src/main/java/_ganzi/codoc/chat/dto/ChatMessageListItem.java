package _ganzi.codoc.chat.dto;

import _ganzi.codoc.chat.enums.ChatMessageType;
import java.time.Instant;

public record ChatMessageListItem(
        Long messageId,
        Long senderId,
        String senderNickname,
        String senderAvatarImageUrl,
        ChatMessageType type,
        String content,
        Instant createdAt) {}
