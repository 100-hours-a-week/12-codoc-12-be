package _ganzi.codoc.chat.dto;

import _ganzi.codoc.chat.domain.ChatRoom;
import java.time.Instant;

public record ChatRoomListItem(
        Long roomId,
        String title,
        boolean hasPassword,
        int participantCount,
        int maxParticipants,
        Instant lastMessageAt) {

    public static ChatRoomListItem from(ChatRoom chatRoom, int maxParticipants) {
        return new ChatRoomListItem(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getPassword() != null,
                chatRoom.getParticipantCount(),
                maxParticipants,
                chatRoom.getLastMessageAt());
    }
}
