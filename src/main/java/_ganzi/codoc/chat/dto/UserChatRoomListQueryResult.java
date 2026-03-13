package _ganzi.codoc.chat.dto;

import java.time.Instant;

public record UserChatRoomListQueryResult(
        Long participantId,
        Long roomId,
        String title,
        int participantsCount,
        String lastMessagePreview,
        Instant lastMessageAt) {}
