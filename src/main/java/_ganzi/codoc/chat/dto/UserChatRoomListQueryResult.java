package _ganzi.codoc.chat.dto;

import java.time.Instant;

public record UserChatRoomListQueryResult(
        Long participantId,
        Long roomId,
        String title,
        long participantsCount,
        String lastMessagePreview,
        Instant lastMessageAt) {}
