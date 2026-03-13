package _ganzi.codoc.chat.dto;

import java.time.Instant;

public record ChatRoomListQueryResult(
        Long roomId, String title, boolean hasPassword, long participantCount, Instant lastMessageAt) {}
