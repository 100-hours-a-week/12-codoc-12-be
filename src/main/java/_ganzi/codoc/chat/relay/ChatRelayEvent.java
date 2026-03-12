package _ganzi.codoc.chat.relay;

import _ganzi.codoc.chat.dto.ChatMessageBroadcast;
import _ganzi.codoc.chat.dto.ChatRoomUpdateBroadcast;
import java.time.Instant;
import java.util.UUID;

public record ChatRelayEvent(
        String eventId,
        String sourceServerId,
        ChatRelayEventType type,
        Long roomId,
        Long userId,
        ChatMessageBroadcast message,
        ChatRoomUpdateBroadcast roomUpdate,
        Instant publishedAt) {

    public static ChatRelayEvent roomMessage(
            String sourceServerId, Long roomId, ChatMessageBroadcast message) {
        return new ChatRelayEvent(
                UUID.randomUUID().toString(),
                sourceServerId,
                ChatRelayEventType.ROOM_MESSAGE,
                roomId,
                null,
                message,
                null,
                Instant.now());
    }

    public static ChatRelayEvent roomListUpdate(
            String sourceServerId, Long userId, ChatRoomUpdateBroadcast roomUpdate) {
        return new ChatRelayEvent(
                UUID.randomUUID().toString(),
                sourceServerId,
                ChatRelayEventType.ROOM_LIST_UPDATE,
                null,
                userId,
                null,
                roomUpdate,
                Instant.now());
    }
}
