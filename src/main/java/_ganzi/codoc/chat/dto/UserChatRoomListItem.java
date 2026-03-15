package _ganzi.codoc.chat.dto;

import java.time.Instant;

public record UserChatRoomListItem(
        Long roomId,
        String title,
        int participantsCount,
        String lastMessagePreview,
        Instant lastMessageAt,
        long unreadCount) {}
