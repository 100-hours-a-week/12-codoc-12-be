package _ganzi.codoc.chat.relay;

import _ganzi.codoc.chat.dto.ChatMessageBroadcast;
import _ganzi.codoc.chat.dto.ChatReadAckBroadcast;
import _ganzi.codoc.chat.dto.ChatRoomUpdateBroadcast;
import _ganzi.codoc.chat.dto.ChatUnreadStatusBroadcast;
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
        ChatUnreadStatusBroadcast unreadStatus,
        ChatReadAckBroadcast readAck,
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
                null,
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
                null,
                null,
                Instant.now());
    }

    public static ChatRelayEvent unreadStatusUpdate(
            String sourceServerId, Long userId, ChatUnreadStatusBroadcast unreadStatus) {
        return new ChatRelayEvent(
                UUID.randomUUID().toString(),
                sourceServerId,
                ChatRelayEventType.UNREAD_STATUS_UPDATE,
                null,
                userId,
                null,
                null,
                unreadStatus,
                null,
                Instant.now());
    }

    public static ChatRelayEvent readAck(
            String sourceServerId, Long roomId, ChatReadAckBroadcast readAck) {
        return new ChatRelayEvent(
                UUID.randomUUID().toString(),
                sourceServerId,
                ChatRelayEventType.READ_ACK,
                roomId,
                null,
                null,
                null,
                null,
                readAck,
                Instant.now());
    }
}
