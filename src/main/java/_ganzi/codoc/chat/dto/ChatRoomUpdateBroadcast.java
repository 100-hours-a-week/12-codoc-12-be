package _ganzi.codoc.chat.dto;

import java.time.Instant;

public record ChatRoomUpdateBroadcast(
        Long roomId, String lastMessagePreview, Instant lastMessageAt) {}
